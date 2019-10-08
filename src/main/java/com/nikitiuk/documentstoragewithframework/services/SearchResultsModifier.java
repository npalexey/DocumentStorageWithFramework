package com.nikitiuk.documentstoragewithframework.services;

import com.nikitiuk.documentstoragewithframework.entities.DocBean;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchResultsModifier {

    private static final Logger logger = LoggerFactory.getLogger(SearchResultsModifier.class);

    public static StringBuilder getSearchResultForPermittedDocs(QueryResponse response, String query, List<DocBean> permittedDocs) {
        List<String> docPaths = getPaths(permittedDocs);
        Map<String, Map<String, List<String>>> hitHighlightedMap = getHighlightedMap(response, docPaths);
        return buildSearchResponse(hitHighlightedMap, query);
    }

    private static List<String> getPaths(List<DocBean> permittedDocs) {
        List<String> docPaths = new ArrayList<>();
        for (DocBean docBean : permittedDocs) {
            docPaths.add(docBean.getPath());
        }
        return docPaths;
    }

    private static Map<String, Map<String, List<String>>> getHighlightedMap(QueryResponse response, List<String> docPaths) {
        Map<String, Map<String, List<String>>> hitHighlightedMap = new HashMap<>();
        for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
            if (docPaths.contains(entry.getKey())) {
                hitHighlightedMap.put(entry.getKey(), entry.getValue());
            }
        }
        return hitHighlightedMap;
    }

    private static StringBuilder buildSearchResponse(Map<String, Map<String, List<String>>> hitHighlightedMap, String query) {
        StringBuilder contentBuilder = new StringBuilder();
        contentBuilder
                .append("Query request: ").append(query)
                .append("\nFound ").append(hitHighlightedMap.size()).append(" document(s)");
        for (Map.Entry<String, Map<String, List<String>>> entry : hitHighlightedMap.entrySet()) {
            contentBuilder.append("\n\n\n").append("-------------------------------------------------")
                    .append("In document: ").append(entry.getKey()).append("\n\n\n")
                    .append(entry.getValue().get("_text_"));
        }
        return contentBuilder;
    }
}