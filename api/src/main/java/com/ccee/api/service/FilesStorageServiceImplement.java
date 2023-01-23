package com.ccee.api.service;

import java.io.*;
import java.net.MalformedURLException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Stream;

import com.ccee.api.Agente;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Service
public class FilesStorageServiceImplement implements FilesStorageService {

    private final Path root = Paths.get("src/main/resources/uploads");

    private static final String PATHNAME = "src/main/resources/uploads/";

    private static final String FILENAME = "src/main/resources/uploads/staff-simple.xml";
    // xslt for pretty print only, no special task
    // private static final String FORMAT_XSLT = "src/main/resources/uploads/staff-format.xslt";

    @Override
    public void init() {
        try {
            Files.createDirectory(root);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível inicializar a pasta para upload!");
        }
    }

    @Override
    public void save(MultipartFile file) {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(file.getOriginalFilename()));
        } catch (Exception e) {
            if (e instanceof FileAlreadyExistsException) {
                throw new RuntimeException("Um arquivo com este nome já existe.");
            }
            throw new RuntimeException("Não foi possível armazenar o arquivo. Erro: " + e.getMessage());
        }
    }

    @Override
    public Resource load(String filename) {
        try {
            Path file = root.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Não foi possível ler o arquivo!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }

    @Override
    public void delete(String filename) {
        File file = new File(root + "/" + filename);
        FileSystemUtils.deleteRecursively(file);
    }

    @Override
    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    @Override
    public Stream<Path> loadAll() {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new RuntimeException("Não foi possível carregar os arquivos!");
        }
    }

    @Override
    public void outPrintAgentes(String filename) {
        File file = new File(root + "/" + filename);
        ArrayList<Agente> cList = new ArrayList<>();
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            System.out.println("Root element: " + doc.getDocumentElement().getNodeName());
            NodeList nList = doc.getElementsByTagName("agente");
            Agente agente;

            for (int i = 0; i < nList.getLength(); i++) {
                agente = new Agente();
                Node node = nList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element e = (Element) node;
                    agente.setCodigo(Integer.parseInt(e.getElementsByTagName("codigo").item(0).getTextContent()));
                    agente.setData(e.getElementsByTagName("data").item(0).getTextContent());
                    cList.add(agente);
                }
            }
            cList.forEach(ag -> {
                System.out.println("---------------");
                System.out.println("/agentes/agente[]/" + ag.getCodigo());
            });

        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void modifyXmlDomParser(String filename) {
        File file = new File(root + "/" + filename);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try (InputStream is = new FileInputStream(file)) {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(is);
            NodeList listOfAgentes = doc.getElementsByTagName("agente");
            for (int i = 0; i < listOfAgentes.getLength(); i++) {
                Node agente = listOfAgentes.item(i);
                NodeList childNodesOfAgentes = agente.getChildNodes();
                for (int j = 0; j < childNodesOfAgentes.getLength(); j++) {
                    Node itemOfAgente = childNodesOfAgentes.item(j);
                    if (itemOfAgente.getNodeType() == Node.ELEMENT_NODE) {
                        if ("regiao".equalsIgnoreCase(itemOfAgente.getNodeName())) {
                            NodeList childNodesOfRegioes = itemOfAgente.getChildNodes();
                            for (int k = 0; k < childNodesOfRegioes.getLength(); k++) {
                                Node itemOfRegiao = childNodesOfRegioes.item(k);
                                if (itemOfRegiao.getNodeType() == Node.ELEMENT_NODE) {
                                    if ("precoMedio".equalsIgnoreCase(itemOfRegiao.getNodeName())) {
                                         itemOfAgente.removeChild(itemOfRegiao);
                                        // itemOfRegiao.setTextContent("-");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // output to console
            // writeXml(doc, System.out);
            try (FileOutputStream output = new FileOutputStream(file)) {
                writeXml(doc, output);
            }
        } catch (ParserConfigurationException | SAXException
                 | IOException | TransformerException e) {
            e.printStackTrace();
        }
    }

    // write doc to output stream
    private static void writeXml(Document doc, OutputStream output)
            throws TransformerException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(output);
        transformer.transform(source, result);
    }
}