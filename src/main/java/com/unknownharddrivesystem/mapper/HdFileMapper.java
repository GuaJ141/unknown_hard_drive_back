package com.unknownharddrivesystem.mapper;

import com.github.pagehelper.PageRowBounds;
import com.unknownharddrivesystem.entity.HdFile;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


public interface HdFileMapper {

    //文件和文件夹同一个方法
    int fileUpload(HdFile hdFile);

    List<HdFile> selectAllFile(PageRowBounds pageRowBounds, String username, String path);

    List<HdFile> selectFavor(PageRowBounds pageRowBounds, String username);

    List<HdFile> selectTrash(PageRowBounds pageRowBounds, String username);

    HdFile selectFileByID(int id);

    HdFile selectFolderByID(int id);

    HdFile selectFolderByName(int foldername);

    HdFile samePathSameFile(String filename, String fileformat, String username, String path);

    HdFile samePathSameFolder(String filename, String username, String path);

    int fileRename(int id, String filename);

    int folderPathUpdate(int id, String path);

    int moveToTrashZone(int id);

    int fileRestore(int id);

    int moveToFavor(int id);

    int favorRestore(int id);

    int realDelete(int id);

    int deleteFileByUsername (String username);

    List<HdFile> selectFileByPathLike(String username, String path);

    int deleteFolderByPath(String username, String path);

    List<HdFile> selectFileByFilename(PageRowBounds pageRowBounds, String username, String filename);

    List<HdFile> selectFavorFileByFilename(PageRowBounds pageRowBounds, String username, String filename);

    List<HdFile> selectTrashFileByFilename(PageRowBounds pageRowBounds, String username, String filename);




}
