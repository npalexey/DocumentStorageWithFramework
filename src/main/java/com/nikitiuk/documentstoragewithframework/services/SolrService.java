package com.nikitiuk.documentstoragewithframework.services;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.queryparser.complexPhrase.ComplexPhraseQueryParser;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.request.AbstractUpdateRequest;
import org.apache.solr.client.solrj.request.ContentStreamUpdateRequest;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.params.CoreAdminParams;
import org.apache.solr.handler.extraction.ExtractingParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SolrService {

    private static final Logger logger = LoggerFactory.getLogger(SolrService.class);

    public static void indexDocumentWithSolr(String documentPath, String contentType) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        ContentStreamUpdateRequest req = new ContentStreamUpdateRequest("/update/extract");
        req.addFile(new File(documentPath), contentType); //application/octet-stream text/plain pdf
        req.setParam("literal.docpath", documentPath);
        req.setParam("literal.docname", documentPath.substring(documentPath.lastIndexOf("/") + 1));
        req.setAction(AbstractUpdateRequest.ACTION.COMMIT, true, true);
        client.request(req);
    }

    public static void deleteDocumentOrRecursiveFolderFromSolrIndex(String path) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        client.deleteByQuery(String.format("docpath:\"%s*\"", path)); // * for 'starts with'
        client.commit();
    }

    public static QueryResponse searchInDocumentsByQuery(String queryString) throws IOException, SolrServerException {
        SolrClient client = new HttpSolrClient.Builder("http://localhost:8983/solr/mycoll").build();
        SolrQuery query = new SolrQuery();
        query.setQuery(String.format("\"%s\"", queryString));
        query.setHighlight(true);
        //query.setParam("hl", "on");                                    //same as .setHighlight(true);
        query.setParam("hl.method", "original");          //default original, other: unified, fastVector
        //query.setParam("hl.bs.type", "LINE");                          //if unified method was chosen
        //query.setParam("hl.mergeContiguous", "true");
        query.setParam("hl.simple.pre", " <span style=\"color: #011DFE; font-weight:bold;\">");  //default <em>, hl.tag.pre for other than original
        query.setParam("hl.simple.post", "</span> ");     //default </em>, hl.tag.post for other
        query.setParam("hl.fl", "*");                     //default *, fields to search in
        query.setParam("hl.encoder", "");                 //default html
        query.setParam("hl.snippets", "10");              //default 1
        query.setParam("hl.maxAnalyzedChars", "1000000"); //default 50000
        query.setParam("hl.fragsize", "100");             //default 100
        //query.setParam("hl.usePhraseHighlighter", "false");            //default true
        //query.setStart(0);
        query.setRows(50);
        return client.query(query);
    }

    public static void listCoresInSolr() throws IOException, SolrServerException {
        SolrClient client2 = new HttpSolrClient.Builder("http://localhost:8983/solr/").build();
        CoreAdminRequest request = new CoreAdminRequest();
        request.setAction(CoreAdminParams.CoreAdminAction.STATUS);
        CoreAdminResponse cores = request.process(client2);
        // List of the cores
        List<String> coreList = new ArrayList<>();
        for (int i = 0; i < cores.getCoreStatus().size(); i++) {
            coreList.add(cores.getCoreStatus().getName(i));
        }
        System.out.println(coreList.toString());
    }
}
