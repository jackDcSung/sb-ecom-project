package com.ecommerce.project.service;


import com.ecommerce.project.exceptions.APIException;
import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Cart;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.CartDTO;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repositories.CartRepository;
import com.ecommerce.project.repositories.CategoryRepository;
import com.ecommerce.project.repositories.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    private CartRepository cartRepository;


    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private FileService fileService;


    @Value("${project.image}")
    private String path;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));
        //check if product already present or not

        boolean isProductNotPresent = true;

        List<Product> products = category.getProducts();

        for (Product value : products) {

            if (value.getProductName().equals(productDTO.getProductName())) {

                isProductNotPresent = false;

                break;

            }
        }

        if (isProductNotPresent) {


            //transfer here
            Product product = modelMapper.map(productDTO, Product.class);


            product.setImage("default.png");
            product.setCategory(category);


            double specialPrice = product.getPrice() -
                    ((product.getDiscount() * 0.01) * product.getPrice());


            product.setSpecialPrice(specialPrice);


            Product savedProduct = productRepository.save(product);


            return modelMapper.map(savedProduct, ProductDTO.class);

        } else {

            throw
                    new APIException("PRoduct already exist");
        }
    }

    @Override
    public ProductResponse gettAllProducts(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findAll(pageDetails);

        //不再從產品庫中獲取所有項目


        //for trasformation
        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());


        if (products.isEmpty()) {

            throw new APIException("No PRoducts Exist!!");
        }


        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);

        productResponse.setPageNumber(pageProducts.getNumber());

        productResponse.setPageSize(pageProducts.getSize());

        productResponse.setTotalElement(pageProducts.getTotalElements());

        productResponse.setLastPage(pageProducts.isLast());

        return productResponse;


    }

    @Override
    public ProductResponse searchByCategory(Long categoryId, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByCategoryOrderByPriceAsc(category, pageDetails);

        //不再從產品庫中獲取所有項目


        //for trasformation
        List<Product> products = pageProducts.getContent();


        if(products.isEmpty()){

            throw  new APIException(category.getCategoryName()+"category does not have any product");
        }


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());


        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);


        return productResponse;


    }

    @Override
    public ProductResponse serachProductBykeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {


        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();


        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<Product> pageProducts = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%', pageDetails);


        List<Product> products = pageProducts.getContent();

        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .toList();

        if(products.isEmpty()){

            throw  new APIException("Product not found with keyword: "+keyword);
        }



        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);


        productResponse.setContent(productDTOS);

        productResponse.setPageNumber(pageProducts.getNumber());

        productResponse.setPageSize(pageProducts.getSize());

        productResponse.setTotalElement(pageProducts.getTotalElements());

        productResponse.setLastPage(pageProducts.isLast());


        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {


        //get the existing  product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        Product product = modelMapper.map(productDTO, Product.class);










        //update the product info with user shared
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());

        productFromDb.setSpecialPrice(product.getSpecialPrice());
        //save to database
        Product savedProduct = productRepository.save(productFromDb);

        //after saving product
        List<Cart> carts = cartRepository.findCartsByProductId(productId);

        List<CartDTO> cartDTOs = carts.stream().map(cart -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);

            List<ProductDTO> products = cart.getCartItems().stream()
                    .map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).collect(Collectors.toList());

            cartDTO.setProducts(products);

            return cartDTO;

        }).collect(Collectors.toList());

        cartDTOs.forEach(cart -> cartService.updateProductInCarts(cart.getCartId(), productId));

        return modelMapper.map(savedProduct, ProductDTO.class);

    }

    @Override
    public ProductDTO deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));

        //delete
        List<Cart> carts=cartRepository.findCartsByProductId(productId);

        carts.forEach(cart -> cartService.deleteProductFromCart(cart.getCartId(), productId));





        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long producctId, MultipartFile image) throws IOException {


        //Get the product from FB
        Product productFromDb = productRepository.findById(producctId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Product", "productId", producctId));


        //Upload image to server
        //Get the file name of uploaded image
        //after user application.property


        //userfileserice
        String fileName = fileService.uploadImage(path, image);

        //updating the new file name th the product

        productFromDb.setImage(fileName);


        //Save updated product
        Product updatedProduct = productRepository.save(productFromDb);


        //return DTO after mapping product to DTO
        return modelMapper.map(updatedProduct, ProductDTO.class);


    }


}
