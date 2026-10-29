package com.fitness.fitness.controller;

import com.fitness.fitness.dto.CommentDTO;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.service.CommunityService;
import com.fitness.fitness.util.UploadFileUtil;
import com.fitness.fitness.vo.CommentVO;
import com.fitness.fitness.vo.CommunityVO;
import com.fitness.fitness.vo.Result;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/community")
public class CommunityController {

    @Autowired
    private CommunityService communityService;

    @Autowired
    private UploadFileUtil fileUtil;

    /**
     * 发布动态+上传图片（最多 9 张，逗号分隔存入 img_url）
     */
    @PostMapping("/publish")
    public Result<Void> publish(@RequestParam String content,
                                @RequestParam(required = false) MultipartFile[] imgs,
                                HttpServletRequest request) throws IOException {
        Long userId = (Long) request.getAttribute("loginUserId");
        List<String> urls = new ArrayList<>();
        if (imgs != null) {
            if (imgs.length > 9) {
                throw new BusinessException("最多上传 9 张图片");
            }
            for (MultipartFile img : imgs) {
                if (img != null && !img.isEmpty()) {
                    urls.add(fileUtil.uploadImg(img));
                }
            }
        }
        String imgUrl = urls.isEmpty() ? null : String.join(",", urls);
        communityService.publish(content, imgUrl, userId);
        return Result.success(null, "动态发布成功");
    }

    /**
     * 分页查询社区全部动态
     */
    @GetMapping("/page")
    public Result<Page<CommunityVO>> page(@RequestParam(defaultValue = "1") Long pageNum,
                                          @RequestParam(defaultValue = "10") Long pageSize) {
        Page<CommunityVO> page = communityService.pageAll(pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 点赞动态
     */
    @PostMapping("/like/{id}")
    public Result<Void> like(@PathVariable Long id) {
        communityService.addLike(id);
        return Result.success(null, "点赞成功");
    }

    /**
     * 发表评论
     */
    @PostMapping("/comment/{dynamicId}")
    public Result<Void> comment(@PathVariable Long dynamicId,
                                @Valid @RequestBody CommentDTO dto,
                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        communityService.addComment(dynamicId, dto.getContent(), userId);
        return Result.success(null, "评论成功");
    }

    /**
     * 分页查询某条动态的评论
     */
    @GetMapping("/comments")
    public Result<Page<CommentVO>> comments(@RequestParam Long dynamicId,
                                            @RequestParam(defaultValue = "1") Long pageNum,
                                            @RequestParam(defaultValue = "10") Long pageSize) {
        return Result.success(communityService.pageComments(dynamicId, pageNum, pageSize));
    }

    /**
     * 删除本人评论
     */
    @DeleteMapping("/comment/{id}")
    public Result<Void> deleteComment(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        communityService.deleteMyComment(id, userId);
        return Result.success(null, "删除成功");
    }

    /**
     * 删除本人动态
     */
    @DeleteMapping("/delete/{id}")
    public Result<Void> delete(@PathVariable Long id, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("loginUserId");
        communityService.deleteMyDynamic(id, userId);
        return Result.success(null, "删除成功");
    }
}