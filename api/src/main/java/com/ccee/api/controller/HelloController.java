package com.ccee.api.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;

import com.ccee.api.model.FileInfo;
import com.ccee.api.message.ResponseMessage;
import com.ccee.api.service.FilesStorageService;

@RestController
@CrossOrigin("http://localhost:4200")
public class HelloController {

    @Autowired
    FilesStorageService storageService;

    @GetMapping("/test")
    public void test() throws IOException {
        storageService.modifyXmlDomParser("exemplo_01.xml");
    }

    @GetMapping("/hello")
    public void hello() throws IOException {
        Resource file = storageService.load("exemplo_01.xml");
        System.out.println(file.getFile().toString());
    }

    @PostMapping("/upload")
    public ResponseEntity<ResponseMessage> uploadFiles(@RequestParam("files") MultipartFile[] files) {
        String message = "";
        try {
            List<String> fileNames = new ArrayList<>();
            Arrays.asList(files).stream().forEach(file -> {
                storageService.delete(file.getOriginalFilename());
                storageService.save(file);
                storageService.modifyXmlDomParser(file.getOriginalFilename());
                storageService.outPrintAgentes(file.getOriginalFilename());
                fileNames.add(file.getOriginalFilename());
            });
            message = "Arquivos carregados com sucesso: " + fileNames;
            return ResponseEntity.status(HttpStatus.OK).body(new ResponseMessage(message));
        } catch (Exception e) {
            message = "Falha ao enviar o(s) arquivo(s)! Error: " + e.getMessage();
            return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED).body(new ResponseMessage(message));
        }
    }

    @GetMapping("/files")
    public ResponseEntity<List<FileInfo>> getListFiles() {
        List<FileInfo> fileInfos = storageService.loadAll().map(path -> {
            String filename = path.getFileName().toString();
            storageService.outPrintAgentes(filename);
            String url = MvcUriComponentsBuilder
                    .fromMethodName(HelloController.class, "getFile", path.getFileName().toString()).build().toString();
            return new FileInfo(filename, url);
        }).collect(Collectors.toList());

        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    @GetMapping("/files/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        storageService.outPrintAgentes(filename);
        Resource file = storageService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"").body(file);
    }
}
