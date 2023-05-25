package com.dynamo.repository;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.dynamo.models.Movie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBScanExpression;


import java.util.List;

@Repository
public class MovieRepository {
    private final String tableName = "moviess";

    @Autowired
    private DynamoDBMapper dynamoDBMapper;
    public Movie save(Movie movie){
        dynamoDBMapper.save(movie);
        return movie;
    }

    public List<Movie> saveAll(List<Movie> bookList){
        dynamoDBMapper.batchSave(bookList);
        return bookList;
    }

    public Movie findById(String id){
        return dynamoDBMapper.load(Movie.class, id);
    }

    public List<Movie> findAll(){
        return dynamoDBMapper.scan(Movie.class, new DynamoDBScanExpression());
    }

//    public String update(String id, Movie movie){
//        dynamoDBMapper.save(movie,
//                new DynamoDBSaveExpression()
//                        .withExpectedEntry("id",
//                                new ExpectedAttributeValue(
//                                        new AttributeValue().withS(id)
//                                )));
//        return id;
//    }

    public String delete(String id){
        Movie book = dynamoDBMapper.load(Movie.class, id);
        dynamoDBMapper.delete(book);
        return "Movie deleted successfully:: "+id;
    }


}

