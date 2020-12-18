package com.vooglooscr;


import com.epam.indigo.Indigo;
import com.epam.indigo.IndigoObject;
import com.epam.indigo.elastic.ElasticRepository;
import com.epam.indigo.model.FromIndigoObject;
import com.epam.indigo.model.Helpers;
import com.epam.indigo.model.IndigoRecord;
import com.epam.indigo.predicate.ExactMatch;
import com.epam.indigo.predicate.SimilarityMatch;
import com.epam.indigo.predicate.SubstructureMatch;
import org.apache.commons.lang3.time.StopWatch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    protected static ElasticRepository<IndigoRecord> repository;

    public static void main(String[] args) {
        setElasticRepository();

//        indexDrugBank();
//        indexChEMBL();
        search();

        System.out.println("Done");
    }

    public static void setElasticRepository() {
        ElasticRepository.ElasticRepositoryBuilder<IndigoRecord> builder = new ElasticRepository.ElasticRepositoryBuilder<>();
        repository = builder
                .withHostName("192.168.1.60")
//                .withHostName("localhost")
                .withPort(9200)
                .withScheme("http")
                .withIndexName("chembl")
//                .withIndexName("drug-bank")
                .withUserName("ez-kibana")
                .withPassword("PHmLHGCmg2G3UzM")
                .build();
    }

    public static void indexDrugBank() {
        String sdfFile = "src/main/resources/open-structures.sdf";

        try {
            List<IndigoRecord> records = Helpers.loadFromSdf(sdfFile);
            System.out.println("Parsed " + records.size());
            repository.indexRecords(records, records.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void indexChEMBL() {
        String sdfFile = "src/main/resources/chembl_27.sdf";

        try {
            System.out.println("Start parsed " + sdfFile + " file");

            Indigo indigo = new Indigo();
            List<IndigoRecord> records = new ArrayList();

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
                        System.out.println("Indexing " + i + "records");
                    }
                } catch (Exception ex) {

                }
            }
            if (records.size() > 0) {
                i += records.size();
                repository.indexRecords(records, 100);
                System.out.println("Indexing " + i + "records");
            }


//            List<IndigoRecord> records = Helpers.loadFromSdf(sdfFile);
//            System.out.println("Parsed " + records.size());
//            repository.indexRecords(records, 20);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void search() {
        String smileFilter = "CC(=O)OC1C=CC=CC=1C(O)=O";
        float threshold = 0.9f;
        Integer limit = 100;

        Indigo indigo = new Indigo();
        IndigoObject indigoObject = indigo.loadMolecule(smileFilter);

        IndigoRecord target = FromIndigoObject.build(indigoObject);

        System.out.println("SMILE filter: " + smileFilter);

        System.out.println("Similar search start (threshold = " + threshold + "; limit = " + limit + ")");

        StopWatch sw = new StopWatch();
        sw.start();

        List<IndigoRecord> similarRecords = repository.stream()
                .filter(new SimilarityMatch<>(target, threshold))
                .limit(limit)
                .collect(Collectors.toList());

        sw.stop();
        System.out.println("Records found: " + similarRecords.size());
        System.out.println("Elapsed time: " + sw.toString());



        List<String> molecules = new ArrayList();

        for (IndigoRecord indigoRecord : similarRecords) {
            IndigoObject tmpIndigoObject = indigoRecord.getIndigoObject(indigo);
            String mol = tmpIndigoObject.molfile();
            molecules.add(mol);
        }

        sw.reset();
        sw.start();
        System.out.println("Exact search start (limit = " + limit + ")");

        List<IndigoRecord> exactRecords = repository.stream()
                .filter(new ExactMatch<>(target))
                .limit(limit)
                .collect(Collectors.toList())
                .stream()
                .filter(ExactMatch.exactMatchAfterChecker(target, indigo))
                .collect(Collectors.toList());

        sw.stop();
        System.out.println("Records found: " + exactRecords.size());
        System.out.println("Elapsed time: " + sw.toString());

        sw.reset();
        sw.start();
        System.out.println("Substructure Match search start (limit = " + limit + ")");

        List<IndigoRecord> substructureMatchRecords = repository.stream()
                .filter(new SubstructureMatch<>(target))
                .limit(limit)
                .collect(Collectors.toList())
                .stream()
                .filter(SubstructureMatch.substructureMatchAfterChecker(target, indigo))
                .collect(Collectors.toList());
        sw.stop();
        System.out.println("Records found: " + substructureMatchRecords.size());
        System.out.println("Elapsed time: " + sw.toString());
    }
}
