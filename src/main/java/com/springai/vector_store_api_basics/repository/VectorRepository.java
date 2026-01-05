// This line defines the "package" where this file belongs.
// A package is like a folder that groups related files together.
// Here, the package is called "com.springai.vector_store_api_basics.repository".
package com.springai.vector_store_api_basics.repository;

// Importing the VectorDocument entity class.
// This represents the "documents" table in the database.
import com.springai.vector_store_api_basics.model.VectorDocument;

// Importing Spring Data JPA interfaces and annotations.
import org.springframework.data.jpa.repository.JpaRepository;   // Provides built-in CRUD operations.
import org.springframework.data.jpa.repository.Query;          // Allows writing custom SQL queries.
import org.springframework.data.repository.query.Param;        // Used to bind method parameters to query parameters.
import org.springframework.stereotype.Repository;              // Marks this interface as a Repository bean.

// Importing Java utilities.
import java.util.List;

// @Repository tells Spring:
// "This interface is a Repository bean that interacts with the database."
@Repository
public interface VectorRepository extends JpaRepository<VectorDocument, String> {

    // Custom SQL query for similarity search using pgvector.
    // @Query allows us to write native SQL queries instead of relying only on JPA methods.
    //
    // Explanation of the query:
    // SELECT id, text, embedding
    // FROM documents
    // ORDER BY embedding <=> cast(:queryVector AS vector)
    // LIMIT :topK
    //
    // - SELECT id, text, embedding → Fetches the document ID, text, and embedding.
    // - FROM documents → From the "documents" table.
    // - ORDER BY embedding <=> cast(:queryVector AS vector)
    //   → This uses the pgvector operator `<=>` which calculates the distance
    //     between the stored embedding and the query embedding.
    //   → Results are ordered by similarity (closest vectors first).
    // - LIMIT :topK → Returns only the top K most similar results.
    //
    // nativeQuery = true → Means this is raw SQL, not JPQL.
    @Query(value = """
        SELECT id, text , embedding
        FROM documents
        ORDER BY embedding <=> cast(:queryVector AS vector)
        LIMIT :topK
        """, nativeQuery = true)
    List<VectorDocument> searchSimilar(
            @Param("queryVector") String queryVector, // The query embedding vector (as a string).
            @Param("topK") int topK                   // Number of top similar results to return.
    );
}