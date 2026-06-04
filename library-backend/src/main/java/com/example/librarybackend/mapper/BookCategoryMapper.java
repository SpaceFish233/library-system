package com.example.librarybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.librarybackend.entity.BookCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookCategoryMapper extends BaseMapper<BookCategory> {
}
