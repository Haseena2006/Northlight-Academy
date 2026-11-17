package com.northlight.academy.repository;
import com.northlight.academy.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CategoryRepository extends JpaRepository<Category, Long> { boolean existsByNameIgnoreCase(String name); }
