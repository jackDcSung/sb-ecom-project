package com.ecommerce.project.service;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.payload.CategoryDTO;
import com.ecommerce.project.payload.CategoryResponse;
import com.ecommerce.project.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    //private List<Category> categories = new ArrayList<>();

    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;


    @Override
    public CategoryResponse getAllCategories(Integer pageNumber,Integer pageSize,String sortBy,String sortOrder) {

        Sort sortByAndOrder=sortOrder.equalsIgnoreCase("asc")
                ?Sort.by(sortBy).ascending()
                :Sort.by(sortBy).descending();


        //靜態工廠方法
        Pageable pageDetails= PageRequest.of(pageNumber,pageSize,sortByAndOrder);

        Page<Category> categoryPage=categoryRepository.findAll(pageDetails);

        List<Category> categories=categoryPage.getContent();


        if (categories.isEmpty()) {
            throw new APIException("No Category created till now");
        }

        List<CategoryDTO> catgegoryDTOS = categories.stream()
                .map(category -> modelMapper.map(category, CategoryDTO.class))
                .toList();


//        CatgegoryDTO catgegoryDTOS=modelMapper.map(categories,CatgegoryDTO.class);


        CategoryResponse categoryResponse = new CategoryResponse();

        categoryResponse.setContent(catgegoryDTOS);

        categoryResponse.setPageNumber(categoryPage.getNumber());

        categoryResponse.setPageSize(categoryPage.getSize());

        categoryResponse.setTotoalElements(categoryPage.getTotalElements());


        categoryResponse.setTotalpages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());



        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        Category category = modelMapper.map(categoryDTO, Category.class);

        Category CategoryFromDb = categoryRepository.findByCategoryName(category.getCategoryName());

        if (CategoryFromDb != null) {
            throw new APIException("Category with the name " + category.getCategoryName() + "already exist!!!");

        }


        Category savedCategory = categoryRepository.save(category);

        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory, CategoryDTO.class);


        return savedCategoryDTO;

    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        categoryRepository.delete(category);
        return modelMapper.map(category,CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {


        Category savedCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Category category=modelMapper.map(categoryDTO,Category.class);

        category.setCategoryId(categoryId);


        savedCategory = categoryRepository.save(category);
        return modelMapper.map(savedCategory, CategoryDTO.class);
    }
}
