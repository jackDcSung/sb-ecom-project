package com.ecommerce.project.service;


import com.ecommerce.project.exceptions.ResourceNotFoundException;
import com.ecommerce.project.model.Category;
import com.ecommerce.project.model.Product;
import com.ecommerce.project.payload.ProductDTO;
import com.ecommerce.project.payload.ProductResponse;
import com.ecommerce.project.repository.CategoryRepository;
import com.ecommerce.project.repository.ProductRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
public class ProductServiceImpl implements ProductService {


    @Autowired
    private ProductRepository productRepository;


    @Autowired
    private CategoryRepository categoryRepository;


    @Autowired
    private ModelMapper modelMapper;


    @Override
    public ProductDTO addProduct(Long categoryId, ProductDTO productDTO) {


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));

        //transfer here
        Product product=modelMapper.map(productDTO,Product.class);




        product.setImage("default.png");
        product.setCategory(category);


        double specialPrice = product.getPrice() -
                ((product.getDiscount() * 0.01) * product.getPrice());


        product.setSpecialPrice(specialPrice);


        Product savedProduct = productRepository.save(product);


        return modelMapper.map(savedProduct, ProductDTO.class);


    }

    @Override
    public ProductResponse gettAllProducts() {


        //for trasformation
        List<Product> products = productRepository.findAll();


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());


        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);


        return productResponse;


    }

    @Override
    public ProductResponse searchByCategory(Long categoryId) {


        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category", "categoryId", categoryId));


        List<Product> products = productRepository.findByCategoryOrderByPriceAsc(category);


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());


        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);


        return productResponse;


    }

    @Override
    public ProductResponse serachProductBykeyword(String keyword) {


        List<Product> products = productRepository.findByProductNameLikeIgnoreCase('%' + keyword + '%');


        List<ProductDTO> productDTOS = products.stream()
                .map(product -> modelMapper.map(product, ProductDTO.class))
                .collect(Collectors.toList());


        ProductResponse productResponse = new ProductResponse();

        productResponse.setContent(productDTOS);

        return productResponse;
    }

    @Override
    public ProductDTO updateProduct(Long productId, ProductDTO productDTO) {


        //get the existing  product from DB
        Product productFromDb = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        Product product= modelMapper.map(productDTO, Product.class);





        //update the product info with user shared
        productFromDb.setProductName(product.getProductName());
        productFromDb.setDescription(product.getDescription());
        productFromDb.setQuantity(product.getQuantity());
        productFromDb.setDiscount(product.getDiscount());
        productFromDb.setPrice(product.getPrice());

        productFromDb.setSpecialPrice(product.getSpecialPrice());
        //save to database
        Product savedProduct = productRepository.save(productFromDb);


        return modelMapper.map(savedProduct, ProductDTO.class);


    }

    @Override
    public ProductDTO deleteProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));


        productRepository.delete(product);

        return modelMapper.map(product, ProductDTO.class);
    }

    @Override
    public ProductDTO updateProductImage(Long producctId, MultipartFile image) throws IOException {


        //Get the product from FB
        Product productFromDb=productRepository.findById(producctId)
                .orElseThrow(()->
                    new ResourceNotFoundException("Product","productId",producctId));


        //Upload image to server
        //Get the file name of uploaded image

        String path="images/";

        String fileName=uploadImage(path,image);

        //updating the new file name th the product

        productFromDb.setImage(fileName);



        //Save updated product
        Product updatedProduct=productRepository.save(productFromDb);




        //return DTO after mapping product to DTO
        return  modelMapper.map(updatedProduct,ProductDTO.class);















    }

    private String uploadImage(String path, MultipartFile file) throws IOException {

        //File names of current/original file(note not use getName())
        String originalFileName=file.getOriginalFilename();


        //Generatea unique file name( //avoid same  file name)
        //random UUID
        String randomId= UUID.randomUUID().toString();
        //ex mat.jpg-->1234-->1234.jpg
        String fileName=randomId.concat(originalFileName.substring(originalFileName.lastIndexOf('.')));

        //for operating system different
        //note use seperate not pathSeparator
        String filePath=path+ File.separator+fileName;



        //Check if path exist and create

        File folder=new File(path);
        if(!folder.exists()){
            folder.mkdir();
        }



        //Upload to server
        Files.copy(file.getInputStream(), Paths.get(filePath));


        return  fileName;





        //returning file name




    }

}
