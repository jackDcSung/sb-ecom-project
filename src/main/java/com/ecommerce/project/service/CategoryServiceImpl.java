package com.ecommerce.project.service;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.payload.CatgegoryDTO;
import com.ecommerce.project.repository.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    //private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponse getAllCategories() {

        List<Category> categories = categoryRepository.findAll();

        if (categories.isEmpty()) {
            throw new APIException("No Category created till now");
        }

        List<CatgegoryDTO> catgegoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CatgegoryDTO.class))
                .toList();


//        CatgegoryDTO catgegoryDTOS=modelMapper.map(categories,CatgegoryDTO.class);


        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setContent(catgegoryDTOS);


        return categoryResponse;
    }

    @Override
    public void createCategory(Category category) {

        Category savedCategory = categoryRepository.findByCategoryName(category.getCategoryName());

        if (savedCategory != null) {
            throw new APIException("Category with the name " + category.getCategoryName() + "already exist!!!");

        }


        categoryRepository.save(category);
    }

    @Override
    public String deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return "Category with categoryId: " + categoryId + " deleted successfully !!";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        category.setCategoryId(categoryId);
        savedCategory = categoryRepository.save(category);
        return savedCategory;
    }
}
