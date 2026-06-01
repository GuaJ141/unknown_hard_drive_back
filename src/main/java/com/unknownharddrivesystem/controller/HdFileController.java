package com.unknownharddrivesystem.controller;

import com.github.pagehelper.PageRowBounds;
import com.unknownharddrivesystem.entity.*;
import com.unknownharddrivesystem.mapper.HdCommentMapper;
import com.unknownharddrivesystem.mapper.HdFileMapper;
import com.unknownharddrivesystem.mapper.HdPostMapper;
import com.unknownharddrivesystem.mapper.HdUserMapper;
import com.unknownharddrivesystem.utils.PagingResult;
import com.unknownharddrivesystem.utils.RedisUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import tools.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController //标识控制器
@RequestMapping("/file") //控制器的前缀地址
public class HdFileController {
    private final String MainPath = System.getProperty("user.dir") + "\\" + "UserFiles";

    @Autowired
    HdFileMapper hdFileMapper;
    @Autowired
    HdUserMapper hdUserMapper;
    @Autowired
    HdPostMapper hdPostMapper;
    @Autowired
    HdCommentMapper hdCommentMapper;
    @Autowired
    RedisUtil redisUtil;

    @PostMapping("/upload")
    public List<FileUploadResult> upload(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam("username") String username,
            @RequestParam("path") String path,
            @RequestParam("id") int userid) {
        int flagOutOfSpace = 0;
        int flagUploadConfirm = 0;
        int flagFailed = 0;
        int totalFile = 0;

        List<FileUploadResult> resList = new ArrayList<>();
        for(MultipartFile file : files){
            System.out.println(file.getOriginalFilename());
            //往List加入的数据
            FileUploadResult res = new FileUploadResult();
            res.setFileName(file.getOriginalFilename());

            //更新user已使用空间
            HdUser user = hdUserMapper.selectUserById(userid);
            long userused = user.getUsed();
            userused += file.getSize();

            //判断空间是否溢出
            if (userused < user.getSpace()) {
                //没溢出更新数据库数据
                hdUserMapper.userUsedUpdate(userused, userid);
                //处理存入数据库的数据
                StringBuilder filename = new StringBuilder(file.getOriginalFilename());
                StringBuilder fileformat = new StringBuilder();

                for (int i = filename.length() - 1; i >= 0 ; i--) {
                    char ch = filename.charAt(i);
                    fileformat.append(ch);
                    filename.deleteCharAt(i);
                    if (ch == '.'){
                        break;
                    }
                }
                fileformat.reverse();

                String filepath = path.replace(",", "\\") + "\\";
                String type = "File";

                System.out.println("form 定义的name值 = " + file.getName());
                System.out.println("原始文件名 = " + file.getOriginalFilename());

                HdFile select = hdFileMapper.samePathSameFile(filename.toString(), fileformat.toString(), username, filepath);
                System.out.println("select:" + (select == null));
                if (select == null) {
                    try {
                        HdFile hdFile = new HdFile();
                        hdFile.setFilename(filename.toString());
                        hdFile.setPath(filepath);
                        hdFile.setType(type);
                        hdFile.setUsername(username);
                        hdFile.setSize(file.getSize());
                        hdFile.setFileformat(fileformat.toString());

                        //处理upload_time
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        LocalDateTime now = LocalDateTime.now();
                        String Time = now.format(formatter);
                        Timestamp timestamp = Timestamp.valueOf(Time);
                        hdFile.setUploadTime(timestamp);

                        if (hdFileMapper.fileUpload(hdFile) == 1) {
                            //保存文件到本地
                            file.transferTo(new File(MainPath + "\\" + path.replace(",", "\\") + "\\" + file.getOriginalFilename()));
                            System.out.println("上传成功:" + MainPath + "\\" + path.replace(",", "\\") + "\\" + file.getOriginalFilename());
                            res.setStatus(1);
                        } else {
                            //失败
                            res.setStatus(0);
                        }
                    } catch (Exception e) {
                        System.out.println("上传失败");
                        System.out.println(e.getMessage());
                        //报错
                        res.setStatus(0);
                    }
                } else {
                    //重名
                    res.setStatus(3);
                }
            }else{
                //空间不足
                res.setStatus(2);
            }
            //加入数据
            resList.add(res);
        }

        return resList;
    }

//    @RequestParam("fileformat") String fileformat,
//    @RequestParam("filename") String filename,
//    @RequestParam("path") String path

    //自己的文件下载
    @RequestMapping(value = "/ownerDownload")
    public ResponseEntity<byte[]> ownerDownload(
            HttpServletRequest request,
            @RequestParam("fileId") int fileId,
            @RequestParam("token") String token) throws IOException {
        HttpSession session = request.getSession(false);
        //还没登录，直接返回
        if(session == null) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        HdFile file = hdFileMapper.selectFileByID(fileId);
        HdUser user = (HdUser) session.getAttribute("user");

        //登录用户对不上，返回
        if(!file.getUsername().equals(user.getUsername()))return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        //token对不上，直接返回
        if(!user.getFileAccessToken().equals(token)) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        return download(file);
    }

    //分享的文件下载
    @RequestMapping(value = "/shareDownload")
    public ResponseEntity<byte[]> shareDownload(
            @RequestParam("fileId") int fileId,
            @RequestParam("token") String token) throws IOException {
        String key = String.valueOf(fileId);
        HdFile fileMainInfo = hdFileMapper.selectFileByID(fileId);

        ObjectMapper jackson = new ObjectMapper();
        ShareFile file = jackson.readValue(redisUtil.get(key).toString(), ShareFile.class);

        //mysql不存在文件直接返回
        if(fileMainInfo == null)return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        //redis不存在文件直接返回
        if(file == null)return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        //token不一致直接返回
        if(!token.equals(file.getToken())) return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        return download(fileMainInfo);
    }

    //分享文件下载
    @RequestMapping(value = "/postDownload")
    public ResponseEntity<byte[]> postDownload(
            @RequestParam("postId") int postId,
            @RequestParam("fileId") int fileId,
            @RequestParam("token") String token) throws Exception {
        //通过id找文件相关信息
        HdFile file = hdFileMapper.selectFileByID(fileId);
        HdPost post = hdPostMapper.selectById(postId);

        //文件分享取消，链接关闭
        if(post.getStatus() == 0)return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();
        //如果token对不上，直接返回
        if(!post.getToken().equals(token))return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).build();

        return download(file);
    }

    private ResponseEntity<byte[]> download(HdFile file) throws IOException {

        FileInputStream is = new FileInputStream(MainPath + "\\" + file.getPath() + file.getFilename() + file.getFileformat());
        byte[] bytes=new byte[is.available()];
        //一次读取bytes.length个字节 并将读到的字节放入bytes数组中
        is.read(bytes);
        is.close();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("Content-Disposition","attachment;filename="+ file.getFilename() + file.getFileformat());
        //将要下载的文件的字节流返回给浏览器
        ResponseEntity res = new ResponseEntity<byte[]>(bytes, httpHeaders, HttpStatus.OK);
        return res;
    }

    //新建文件夹
    @PostMapping("/folderCreate")
    public int folderNew(
            @RequestParam("folderName") String folderName,
            @RequestParam("username") String username,
            @RequestParam("path") String path) {
        String folderPath = path.replace(",", "\\") + "\\";
        File folder = new File(MainPath + "\\" + path.replace(",", "\\"), folderName);
//        System.out.println(MainPath + "\\" + folderPath + folderName);
//        System.out.println(path);

        //处理upload_time
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String Time = now.format(formatter);
        Timestamp timestamp = Timestamp.valueOf(Time);

        HdFile hdFolder = new HdFile();
        hdFolder.setUsername(username);
        hdFolder.setPath(folderPath);
        hdFolder.setFilename(folderName);
        hdFolder.setType("Folder");
        hdFolder.setUploadTime(timestamp);

        if (!folder.exists()) {
            if (hdFileMapper.fileUpload(hdFolder) == 1) {
                boolean create = folder.mkdir();
                if (create) {
                    System.out.println("文件夹创建成功");
                    return 1;
                } else {
                    System.out.println("文件夹创建失败");
                    return 0;
                }
            } else {
                System.out.println("数据库写入失败");
                return 0;
            }

        } else {
            System.out.println("文件夹已存在");
            return 0;
        }
    }


    @PostMapping("/selectall")
    public PagingResult<HdFile> count(
            @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
            @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
            @RequestParam("username") String username,
            @RequestParam("path") String path) {

        //System.out.println("username: " + username);
        path = path.replace(",", "\\") + "\\";
        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);
        List<HdFile> allFiles = hdFileMapper.selectAllFile(pageRowBounds, username, path);
        //返回课程分页数据，总的记录条数
        //PagingResult<HdFile> res = new PagingResult<HdFile>(allFiles,pageRowBounds.getTotal());
        return new PagingResult<HdFile>(allFiles,pageRowBounds.getTotal());

    }

    //文件重命名
    @Transactional
    @PostMapping("/fileRename")
    public int rename(
            @RequestParam("filename") String newfilename,
            @RequestParam("id") int id,
            @RequestParam("username") String username,
            @RequestParam("type") String type) {

        //通过id查询找到源文件
        new HdFile();
        HdFile oldfile;
        if (Objects.equals(type, "File")){
            oldfile = hdFileMapper.selectFileByID(id);
        }else{
            oldfile = hdFileMapper.selectFolderByID(id);
        }

        //判断新文件名是否重复
        HdFile exits = hdFileMapper.samePathSameFile(newfilename, oldfile.getFileformat(), username, oldfile.getPath());
        if (Objects.equals(type, "File")){
            exits = hdFileMapper.samePathSameFile(newfilename, oldfile.getFileformat(), username, oldfile.getPath());
        }else{
            exits = hdFileMapper.samePathSameFolder(newfilename, username, oldfile.getPath());
        }
        //System.out.println(MainPath + "\\" + oldfile.getPath() + oldfile.getFilename());

        String selectpath = (oldfile.getPath() + oldfile.getFilename()).replace("\\", "\\\\") + "\\\\";

        if (exits == null) {
            File file;
            File newFile;
            if (Objects.equals(type, "File")){
                file = new File(MainPath + "\\" + oldfile.getPath() + oldfile.getFilename() + oldfile.getFileformat());
                newFile = new File(MainPath + "\\" + oldfile.getPath() + newfilename + oldfile.getFileformat());
            }else{
                file = new File(MainPath + "\\" + oldfile.getPath() + oldfile.getFilename());
                newFile = new File(MainPath + "\\" + oldfile.getPath() + newfilename);
            }

            if (file.exists()) {
                if (hdFileMapper.fileRename(id, newfilename) == 1) {
                    boolean renamed = file.renameTo(newFile);
                    if (renamed) {
                        if (Objects.equals(type, "Folder")){
                            List<FolderPath> list = new ArrayList<>();

                            List<HdFile> allfilepath = hdFileMapper.selectFileByPathLike(username, selectpath);
                            for (HdFile i: allfilepath){

                                String res = i.getPath().substring((oldfile.getPath() + oldfile.getFilename()) .length());
                                String finalpath = oldfile.getPath() + newfilename + res;

//                                System.out.println("res: "  + res);
//                                System.out.println("finalpath: "  + finalpath);

                                FolderPath folder = new FolderPath();
                                folder.setId(i.getId());
                                folder.setPath(finalpath);
                                folder.setFilename(i.getFilename());
                                list.add(folder);
                            }

                            for(FolderPath i: list){
                                //System.out.print("id: " + i.getId());
                                //System.out.println("path: " + i.getPath());
                                if(hdFileMapper.folderPathUpdate(i.getId(), i.getPath()) != 1){
                                    throw new RuntimeException("数据更新失败，数据回滚");
                                }
                            }
                        }
                        //System.out.println("重命名成功");
                        return 1;
                    } else {
                        //System.out.println("重命名失败");
                        return 0;
                    }
                } else {
                    //System.out.println("未找到文件");
                    return 0;
                }
            } else {
                //System.out.println("文件夹不存在");
                return 0;
            }
        }
        return 0;
    }

    //移入回收站
    @PostMapping("/moveToTrashZone")
    public int moveToTrashZone(@RequestParam("id") int id){

        int res = hdFileMapper.moveToTrashZone(id);
        return res;
    }

    @PostMapping("/searchTrash")
    public PagingResult<HdFile> searchTrash(
            @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
            @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
            @RequestParam("search") String search,
            @RequestParam("username") String username) {
        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);

        List<HdFile> allfile = hdFileMapper.selectTrashFileByFilename(pageRowBounds, username, search);
        return new PagingResult<HdFile>(allfile,pageRowBounds.getTotal());

    }

    //真删除
    @PostMapping("/delete")
    public int realDelete(
            @RequestParam("id") int id,
            @RequestParam("type") String type){

        boolean tag = true;
        HdFile hdFile;

        //找到文件夹或文件的数据库信息
        List<HdFile> allfileid = new ArrayList<>();
        if (Objects.equals(type, "File")){
            hdFile = hdFileMapper.selectFileByID(id);
        }else{
            hdFile = hdFileMapper.selectFolderByID(id);
        }

        HdUser user = hdUserMapper.selectUserByUsername(hdFile.getUsername());

        //格式化路径
        String selectpath = (hdFile.getPath() + hdFile.getFilename()).replace("\\", "\\\\");

        //找到文件下的所有文件
        List<HdFile> allfilepath = hdFileMapper.selectFileByPathLike(hdFile.getUsername(), selectpath);

        //删除文件夹或文件
        File file;
        if (Objects.equals(type, "File")){
            file = new File(MainPath + "\\" + hdFile.getPath() + hdFile.getFilename() + hdFile.getFileformat());
        }else{
            file = new File(MainPath + "\\" + hdFile.getPath() + hdFile.getFilename());
        }
        //System.out.println(MainPath + "\\" + hdFile.getPath() + hdFile.getFilename());

        if (Objects.equals(type, "File")){
            if (file.exists()){
                boolean isDelete = file.delete();

                if (isDelete){
                    //System.out.println("文件删除成功");
                    int res = hdFileMapper.realDelete(id);

                    //删除文章
                    HdPost posts = hdPostMapper.isTheFilePosted(id);
                    if (posts != null){
                        hdPostMapper.delPost(posts.getId());

                        List<HdComment> comments = hdCommentMapper.selectComment(posts.getId());
                        if (!comments.isEmpty()){
                            for (HdComment i: comments){
                                hdCommentMapper.delComment(i.getId());
                            }
                        }
                    }

                    if (res >= 1){
                        //System.out.println("数据库数据删除成功");

                        //更新用户已用空间
                        if (hdUserMapper.userUsedUpdate((user.getUsed() - hdFile.getSize()), user.getId()) == 1){
                            //System.out.println("用户使用空间更改成功");
                        }else{
                            return 0;
                        }

                        return res;
                    }else{
                        //System.out.println("数据库数据删除失败");
                        return 0;
                    }
                }else{
                    //System.out.println("文件删除失败");
                    return 0;
                }
            }else{
                //System.out.println("文件不存在");
                return 0;
            }
        }else{
            if (fileDelete(file)){
                long allTakenSpace = 0;

                for (HdFile i : allfilepath) {
                    //数据库数据删除
                    int isdelete = hdFileMapper.realDelete(i.getId());
                    allTakenSpace += i.getSize();
                    if (isdelete <= 0){
                        tag = false;
                    }
                }

                int finaldelete = hdFileMapper.realDelete(id);

                //更新用户已用空间
                if (hdUserMapper.userUsedUpdate((user.getUsed() - allTakenSpace), user.getId()) == 1){
                    //System.out.println("用户使用空间更改成功");
                }else{
                    return 0;
                }

                //System.out.println(tag);
                //System.out.println(finaldelete);

                if (tag && finaldelete == 1) {
                    //System.out.println("数据删除完成");
                    return 1;

                } else {
                    //System.out.println("数据删除失败");
                    return 0;
                }
            }
        }
        return 0;
    }

    //只负责文件删除
    public boolean fileDelete(File folder){
        File[] files = folder.listFiles();

        int fileCount = 0;
        fileCount = files.length;
        boolean tag = true;

        for (File file : files) {

            if (file.isDirectory()) {
                fileDelete(file);
                fileCount--;
            } else {
                //文件删除
                boolean isdelete = file.delete();
                if (isdelete) {
                    fileCount--;
                    tag = isdelete;
                    //System.out.println("文件删除成功");
                }
            }
        }

        if (fileCount == 0) {
            boolean folderdelete = new File(folder.getPath()).delete();
            tag = folderdelete;
            //System.out.println("文件夹删除成功");
        }
        return tag;
    }

    @PostMapping("/selecfavor")
    public PagingResult<HdFile> favor(
            @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
            @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
            @RequestParam("username") String username){
        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);

        List<HdFile> allfile = hdFileMapper.selectFavor(pageRowBounds, username);
        return new PagingResult<HdFile>(allfile,pageRowBounds.getTotal());
    }

    @PostMapping("/searchfavor")
    public PagingResult<HdFile> searchfavor(
            @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
            @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
            @RequestParam("search") String search,
            @RequestParam("username") String username){
        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);

        List<HdFile> allfile = hdFileMapper.selectFavorFileByFilename(pageRowBounds, username, search);
        return new PagingResult<HdFile>(allfile,pageRowBounds.getTotal());
    }

    @PostMapping("/addfavor")
    public int addfavor(
            @RequestParam("id") int id) {
        int res = hdFileMapper.moveToFavor(id);
        return res;
    }

    @PostMapping("/selecttrash")
    public PagingResult<HdFile> trash(
            @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
            @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
            @RequestParam("username") String username) {
        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);

        List<HdFile> allfile = hdFileMapper.selectTrash(pageRowBounds, username);
        return new PagingResult<HdFile>(allfile,pageRowBounds.getTotal());

    }

    @PostMapping("/restore")
    public int restore(
            @RequestParam("id") int id) {
        //System.out.println(id);
        int res = hdFileMapper.fileRestore(id);
        return res;
    }

    @PostMapping("/search")
    public PagingResult<HdFile> search(
        @RequestParam(required = false, name = "limit", defaultValue = "32") int limit,
        @RequestParam(required = false,name = "offset",defaultValue = "0") int offset,
        @RequestParam("search") String search,
        @RequestParam("username") String username) {

        PageRowBounds pageRowBounds = new PageRowBounds(offset, limit);
        List<HdFile> allfiles = hdFileMapper.selectFileByFilename(pageRowBounds, username, search);

        return  new PagingResult<HdFile>(allfiles,pageRowBounds.getTotal());
    }

    @PostMapping("/favorcancel")
    public int favorcancel(
            @RequestParam("id") int id) {
        int res = hdFileMapper.favorRestore(id);
        //System.out.println(res);
        return res;
    }

    @PostMapping("/selectone")
    public HdFile selectone(@RequestParam("fileId") int id){
        //System.out.println("ID：" + id);
        return hdFileMapper.selectFileByID(id);
    }

    @PostMapping("/share")
    public int shareFile(
            HttpServletRequest request,
            @RequestParam("fileId") int fileId,
            @RequestParam(required = false,name = "availTime", defaultValue = "-1") long availTime,
            @RequestParam("timeUnit") int option){
        HttpSession session = request.getSession(false);
        if(session == null) return 0;

        ShareFile shareFile = new ShareFile();

        HdUser me = (HdUser) session.getAttribute("user");
        HdFile fileInfo = hdFileMapper.selectFileByID(fileId);

        //文件主键Id为键值
        String key = String.valueOf(fileInfo.getId());
        if(redisUtil.get(key) != null){
            //已有数据
            return 2;
        }

        TimeUnit timeUnit = switch (option) {
            //1：秒  2：分  3：时  4：天  5：永久
            case 1 -> TimeUnit.SECONDS;
            case 2 -> TimeUnit.MINUTES;
            case 3 -> TimeUnit.HOURS;
            case 4 -> TimeUnit.DAYS;
            default -> null;
        };

        shareFile.setFileId(fileId);
        shareFile.setOption(timeUnit);
        shareFile.setAvailTime(availTime);
        shareFile.setToken(UUID.randomUUID().toString().replace("-", ""));

        //文件不是你存的
        if(!me.getUsername().equals(fileInfo.getUsername())) return 0;

        if(option == 5){
            redisUtil.set(key, shareFile);
        }else{
            redisUtil.set(key, shareFile ,availTime ,timeUnit);
        }

        Object isExist = redisUtil.get(key);
        if(isExist != null){
            return 1;
        }else{
            return 0;
        }
    }

    @GetMapping("/selectShareFile")
    public ShareFile selectShareFile(
            HttpServletRequest request,
            @RequestParam("fileId") int fileId){
        HttpSession session = request.getSession(false);
        HdUser me = (HdUser) session.getAttribute("user");
        HdFile fileMainInfo = hdFileMapper.selectFileByID(fileId);

        if (!me.getUsername().equals(fileMainInfo.getUsername())) return null;

        String key = String.valueOf(fileId);
        ObjectMapper jackson = new ObjectMapper();

        Object fileInfo = redisUtil.get(key);
        if(fileInfo != null){
            return jackson.readValue(redisUtil.get(key).toString(), ShareFile.class);
        }else{
            return null;
        }

    }

    @GetMapping("/shareFileCannel")
    public int shareFileCannel(
            HttpServletRequest request,
            @RequestParam("fileId") int fileId){
        HttpSession session = request.getSession(false);
        HdUser me = (HdUser) session.getAttribute("user");
        HdFile fileMainInfo = hdFileMapper.selectFileByID(fileId);

        if (!me.getUsername().equals(fileMainInfo.getUsername())) return 0;
        String key = String.valueOf(fileId);

        redisUtil.delete(key);
        Object isExist = redisUtil.get(key);
        if(isExist == null){
            return 1;
        }else{
            return 0;
        }
    }
}
