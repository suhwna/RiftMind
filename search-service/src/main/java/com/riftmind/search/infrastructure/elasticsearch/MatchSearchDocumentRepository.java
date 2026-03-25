package com.riftmind.search.infrastructure.elasticsearch;

import com.riftmind.search.domain.search.MatchSearchDocument;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface MatchSearchDocumentRepository extends ElasticsearchRepository<MatchSearchDocument, String> {
}
