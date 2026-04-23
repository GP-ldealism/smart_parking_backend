package cn.gp.smartparking.controller;

import cn.gp.smartparking.common.Result;
import cn.gp.smartparking.model.entity.SysDict;
import cn.gp.smartparking.service.SysDictService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/sys-dict")
@Tag(name = "数据字典管理")
public class SysDictController {

    @Resource
    private SysDictService sysDictService;

    @Operation(summary = "获取字典列表")
    @GetMapping("/list")
    public Result<List<SysDict>> getDictList(@RequestParam(required = false) String dictType) {
        List<SysDict> dicts = sysDictService.lambdaQuery()
                .eq(dictType != null, SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getSortOrder)
                .list();
        return Result.success("获取字典列表成功", dicts);
    }

    @Operation(summary = "获取字典详情")
    @GetMapping("/{id}")
    public Result<SysDict> getDictDetail(@PathVariable Long id) {
        SysDict dict = sysDictService.getById(id);
        return Result.success("获取字典详情成功", dict);
    }

    @Operation(summary = "创建字典项")
    @PostMapping
    public Result<SysDict> createDict(@RequestBody SysDict dict) {
        sysDictService.save(dict);
        return Result.success("创建字典项成功", dict);
    }

    @Operation(summary = "更新字典项")
    @PutMapping("/{id}")
    public Result<SysDict> updateDict(@PathVariable Long id, @RequestBody SysDict dict) {
        dict.setId(id);
        sysDictService.updateById(dict);
        SysDict updatedDict = sysDictService.getById(id);
        return Result.success("更新字典项成功", updatedDict);
    }

    @Operation(summary = "删除字典项")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDict(@PathVariable Long id) {
        sysDictService.removeById(id);
        return Result.success("删除字典项成功");
    }

    @Operation(summary = "根据类型获取字典")
    @GetMapping("/type/{dictType}")
    public Result<List<SysDict>> getDictByType(@PathVariable String dictType) {
        List<SysDict> dicts = sysDictService.lambdaQuery()
                .eq(SysDict::getDictType, dictType)
                .orderByAsc(SysDict::getSortOrder)
                .list();
        return Result.success("根据类型获取字典成功", dicts);
    }
}