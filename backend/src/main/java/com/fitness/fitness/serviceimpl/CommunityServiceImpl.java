package com.fitness.fitness.serviceimpl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fitness.fitness.entity.Community;
import com.fitness.fitness.entity.CommunityComment;
import com.fitness.fitness.entity.User;
import com.fitness.fitness.exception.BusinessException;
import com.fitness.fitness.mapper.CommunityCommentMapper;
import com.fitness.fitness.mapper.CommunityMapper;
import com.fitness.fitness.mapper.UserMapper;
import com.fitness.fitness.service.CommunityService;
import com.fitness.fitness.vo.CommentVO;
import com.fitness.fitness.vo.CommunityVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CommunityServiceImpl implements CommunityService {
    @Autowired
    private CommunityMapper communityMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CommunityCommentMapper commentMapper;

    @Override
    public void publish(String content, String imgUrl, Long userId) {
        Community community = new Community();
        community.setUserId(userId);
        community.setContent(content);
        community.setImgUrl(imgUrl);
        community.setLikeCount(0);
        community.setCreateTime(LocalDateTime.now());
        communityMapper.insert(community);
    }

    @Override
    public Page<CommunityVO> pageAll(Long pageNum, Long pageSize) {
        Page<Community> source = communityMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<Community>lambdaQuery().orderByDesc(Community::getCreateTime));

        List<Community> records = source.getRecords();
        Set<Long> userIds = records.stream().map(Community::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> dynamicIds = records.stream().map(Community::getId).toList();
        Map<Long, Long> commentCountMap = dynamicIds.isEmpty() ? Collections.emptyMap()
                : commentMapper.selectList(Wrappers.<CommunityComment>lambdaQuery()
                        .select(CommunityComment::getDynamicId)
                        .in(CommunityComment::getDynamicId, dynamicIds))
                .stream()
                .collect(Collectors.groupingBy(CommunityComment::getDynamicId, Collectors.counting()));

        List<CommunityVO> voList = records.stream().map(item -> {
            CommunityVO vo = new CommunityVO();
            BeanUtil.copyProperties(item, vo);
            User user = userMap.get(item.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname() != null && !user.getNickname().isBlank()
                        ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            vo.setCommentCount(commentCountMap.getOrDefault(item.getId(), 0L).intValue());
            return vo;
        }).toList();

        Page<CommunityVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public void addLike(Long id) {
        communityMapper.addLike(id);
    }

    @Override
    public void deleteMyDynamic(Long id, Long userId) {
        Community dynamic = communityMapper.selectById(id);
        if (dynamic == null || !dynamic.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己发布的动态");
        }
        communityMapper.deleteById(id);
        commentMapper.delete(Wrappers.<CommunityComment>lambdaQuery()
                .eq(CommunityComment::getDynamicId, id));
    }

    @Override
    public void addComment(Long dynamicId, String content, Long userId) {
        if (communityMapper.selectById(dynamicId) == null) {
            throw new BusinessException("动态不存在或已被删除");
        }
        CommunityComment comment = new CommunityComment();
        comment.setDynamicId(dynamicId);
        comment.setUserId(userId);
        comment.setContent(content.trim());
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
    }

    @Override
    public Page<CommentVO> pageComments(Long dynamicId, Long pageNum, Long pageSize) {
        Page<CommunityComment> source = commentMapper.selectPage(new Page<>(pageNum, pageSize),
                Wrappers.<CommunityComment>lambdaQuery()
                        .eq(CommunityComment::getDynamicId, dynamicId)
                        .orderByDesc(CommunityComment::getCreateTime)
                        .orderByDesc(CommunityComment::getId));

        List<CommunityComment> records = source.getRecords();
        Set<Long> userIds = records.stream().map(CommunityComment::getUserId).collect(Collectors.toSet());
        Map<Long, User> userMap = userIds.isEmpty() ? Collections.emptyMap()
                : userMapper.selectBatchIds(userIds).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));

        List<CommentVO> voList = records.stream().map(item -> {
            CommentVO vo = new CommentVO();
            BeanUtil.copyProperties(item, vo);
            User user = userMap.get(item.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname() != null && !user.getNickname().isBlank()
                        ? user.getNickname() : user.getUsername());
                vo.setAvatar(user.getAvatar());
            }
            return vo;
        }).toList();

        Page<CommentVO> result = new Page<>(source.getCurrent(), source.getSize(), source.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Override
    public void deleteMyComment(Long id, Long userId) {
        CommunityComment comment = commentMapper.selectById(id);
        if (comment == null || !comment.getUserId().equals(userId)) {
            throw new BusinessException("只能删除自己的评论");
        }
        commentMapper.deleteById(id);
    }
}
