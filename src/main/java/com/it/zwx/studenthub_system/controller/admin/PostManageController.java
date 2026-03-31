package com.it.zwx.studenthub_system.controller.admin;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.it.zwx.studenthub_system.constant.MessageConstant;
import com.it.zwx.studenthub_system.pojo.dto.PostAddDTO;
import com.it.zwx.studenthub_system.pojo.dto.extend.PostPageDTO;
import com.it.zwx.studenthub_system.pojo.entity.Post;
import com.it.zwx.studenthub_system.pojo.entity.Result;
import com.it.zwx.studenthub_system.service.PostManageService;
import com.it.zwx.studenthub_system.service.UserService;
import com.it.zwx.studenthub_system.utils.AuthUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/post-manage")
@Tag(name = "管理员帖子管理-补全版")
@RequiredArgsConstructor
public class PostManageController {
    private final PostManageService postManageService;
    private final UserService userService;

    @GetMapping("/page")
    @Operation(summary = "帖子分页查询")
    public Result<Page<Post>> page(@ModelAttribute PostPageDTO dto) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        return Result.success(postManageService.page(dto));
    }

    @PostMapping
    @Operation(summary = "新增帖子")
    public Result<Post> add(@RequestBody PostAddDTO dto) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        return Result.success(postManageService.add(dto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑帖子")
    public Result update(@PathVariable Integer id, @RequestBody PostAddDTO dto) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        postManageService.update(id, dto);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "下架/发布帖子")
    public Result updateStatus(@PathVariable Integer id, @RequestParam Integer status) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        postManageService.updateStatus(id, status);
        return Result.success(MessageConstant.UPDATE_SUCCESS);
    }

    @DeleteMapping
    @Operation(summary = "删除帖子")
    public Result delete(@RequestBody List<Integer> ids) {
        AuthUtil.requireAdmin(AuthUtil.currentUser(userService));
        postManageService.delete(ids);
        return Result.success(MessageConstant.DELETE_SUCCESS);
    }
}
