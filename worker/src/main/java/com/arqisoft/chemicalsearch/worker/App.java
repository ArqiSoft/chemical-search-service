package com.arqisoft.chemicalsearch.worker;

import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.elastic.ElasticRepository;
import com.epam.indigo.model.FromIndigoObject;
import com.epam.indigo.model.IndigoRecord;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class App {
    protected static ElasticRepository<IndigoRecord> repository;
    private static String folderForFiles = "/home/files";

    public static void main(String[] args) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        System.out.println(
                dateFormat.format(Calendar.getInstance().getTime()) + " >> Folder for files = " + folderForFiles);

        setElasticRepository();
        File folder = new File(folderForFiles);
        File[] fileList = folder.listFiles((dir, name) -> name.toLowerCase().endsWith(".sdf"));

        if (fileList != null && fileList.length > 0) {
            for (File file : fileList) {
                if (file.exists()) {
                    indexSdf(file.getPath());
                }
            }
        } else {
            System.out.println("No files found to index");
        }

        System.out.println(dateFormat.format(Calendar.getInstance().getTime()) + " >> Done");
    }

    public static void setElasticRepository() {
        ElasticRepository.ElasticRepositoryBuilder<IndigoRecord> builder = new ElasticRepository.ElasticRepositoryBuilder<>();
        repository = builder.withHostName(System.getenv("CS_ELASTICSEARCH_HOST"))
                .withPort(Integer.parseInt(System.getenv("CS_ELASTICSEARCH_PORT")))
                .withScheme(System.getenv("CS_ELASTICSEARCH_SCHEME"))
                .withIndexName(System.getenv("CS_ELASTICSEARCH_INDEX"))
                .withUserName(System.getenv("CS_ELASTICSEARCH_USER"))
                .withPassword(System.getenv("CS_ELASTICSEARCH_PASSWORD")).build();
    }

    public static void indexSdf(String sdfFile) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

            System.out.println(
                    dateFormat.format(Calendar.getInstance().getTime()) + " >> Start parsed " + sdfFile + " file");

            Indigo indigo = new Indigo();
            List<IndigoRecord> records = new ArrayList<>();

            Iterator iter = indigo.iterateSDFile(sdfFile).iterator();

            Integer i = 0;

            while (iter.hasNext()) {
                IndigoObject comp = (IndigoObject) iter.next();

                try {
                    records.add(FromIndigoObject.build(comp));
                    if (records.size() == 100) {
                        repository.indexRecords(records, 100);
                        records.clear();
                        i += 100;
                        System.out.println("Indexing " + i + " records");
                    }
                } catch (Exception ex) {
                }
            }
            if (!records.isEmpty()) {
                i += records.size();
                repository.indexRecords(records, 100);
                System.out.println("Indexing " + i + "records");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
