package com.example.librarybackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.librarybackend.entity.Book;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BookMapper extends BaseMapper<Book> {
}
