package com.ecommerce.project.service;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
@Service
public class FileServiceImpl implements   FileService {





    @Autowired
    private ModelMapper modelMapper;


    @Override
    public String uploadImage(String path, MultipartFile file) throws IOException {

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









    }
















}
