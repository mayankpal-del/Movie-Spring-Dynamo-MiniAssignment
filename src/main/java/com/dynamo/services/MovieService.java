package com.dynamo.services;

import com.dynamo.models.Movie;
import com.dynamo.repository.MovieRepository;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MovieService {
    private static Logger logger = LoggerFactory.getLogger(MovieService.class);

    @Autowired
    private MovieRepository movieRepository;

    public Movie save(Movie movie){
        logger.info("save movie " + this.getClass().getName());
        return movieRepository.save(movie);
    }


        public List<Movie> readCsv(){
        List<Movie> newMovies = new ArrayList<>();
        String[] record =new String[20];
        try {
            Reader reader = Files.newBufferedReader(Paths.get("C:\\Users\\maypal\\Downloads\\movies.csv"));
            CSVReader csvReader = new CSVReader(reader);
            String[] rec;
            while ((rec = csvReader.readNext()) != null) {
                Movie movie = new Movie();
                movie.setImdb_title_id(rec[0]);

                movie.setTitle(rec[1]);
                movie.setOriginal_title(rec[2]);
                movie.setYear(rec[3]);
                movie.setDate_published(rec[4]);
                movie.setGenre(rec[5]);
                movie.setDuration(rec[6]);
                movie.setCountry(rec[7]);
                movie.setLanguage(rec[8]);
                movie.setDirector(rec[9]);
                movie.setWriter(rec[10]);
                movie.setProduction_company(rec[11]);
                movie.setActors(rec[12]);
                movie.setDescription(rec[13]);
                movie.setAvg_vote(rec[14]);
                movie.setVotes(rec[15]);
                movie.setBudget(rec[16]);
                movie.setUsa_gross_income(rec[17]);
                movie.setWorldwide_gross_income(rec[18]);
                movie.setMetascore(rec[19]);
                movie.setReviews_from_users(rec[20]);
                movie.setReviews_from_critics(rec[21]);
                movieRepository.save(movie);
                newMovies.add(movie);
            }
            csvReader.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (CsvValidationException e) {
            throw new RuntimeException(e);
        }
        return newMovies;
    }

    public List<Movie> saveAll(List<Movie> movieList){
        logger.info("save movie " + this.getClass().getName());
        return  movieRepository.saveAll(movieList);
    }

    public Movie findById(String id){
        logger.info("find movie by id" + this.getClass().getName());
        return movieRepository.findById(id);
    }

    public List<Movie> findAll(){
        logger.info("findAll movies " + this.getClass().getName());
        return movieRepository.findAll();
    }

    public String update(String id, Movie movie){
        logger.info("update movie " + this.getClass().getName());
        return movieRepository.update(id, movie);
    }
    public String delete(String id){
        logger.info("Edit Configurations… movie " + this.getClass().getName());
        return movieRepository.delete(id);
    }

    public List<String> getdirector(String director, int startYear, int endYear) {
        String accessKey = "fakeMyKeyId";
        String secretKey = "fakeSecretAccessKey";
        String region = "us-east-1";
        List<String> Titles = new ArrayList<>();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:8000"))
                .build();

        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("moviess")
                    .build();
            ScanResponse response = ddb.scan(scanRequest);
            for (Map<String, AttributeValue> item : response.items()) {
                String title = null;
                String itemDirector = item.get("director").s();
                if(director.equals(itemDirector) ){
                    int year = Integer.parseInt(item.get("year").s());
                    if(startYear < year && endYear > year) {
                        title = item.get("title").s();
                        Titles.add(title);
                    }
                }
            }

        } catch (
                DynamoDbException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return Titles;
    }


    public List<String> getEnglishTitlesWithUserReviewsGreaterThan(int userReviewFilter) {
        String accessKey = "fakeMyKeyId";
        String secretKey = "fakeSecretAccessKey";
        String region = "mumbai";
        List<String> titles = new ArrayList<>();

        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKey, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);

        DynamoDbClient ddb = DynamoDbClient.builder()
                .credentialsProvider(credentialsProvider)
                .region(Region.of(region))
                .endpointOverride(URI.create("http://localhost:8000"))
                .build();
        try {
            ScanRequest scanRequest = ScanRequest.builder()
                    .tableName("moviess")
                    .build();
            ScanResponse response = ddb.scan(scanRequest);
            List<Map<String, AttributeValue>> items = response.items();


            List<Map<String, AttributeValue>> filteredItems = items.stream()
                    .filter(item -> {
                        AttributeValue languageValue = item.get("language");
                        AttributeValue reviewsValue = item.get("reviews_from_users");


                        if (languageValue != null && reviewsValue != null) {
                            String language = languageValue.s();
                            String reviews = reviewsValue.s();
                            if ("English".equals(language) && Integer.parseInt(reviews) > userReviewFilter)
                                System.out.println(item);
                            return "English".equals(language) && Integer.parseInt(reviews) > userReviewFilter;
                        }
                        return false;
                    })
                    .collect(Collectors.toList());
            filteredItems.sort((item1, item2) -> Integer.compare(
                    Integer.parseInt(item2.get("reviews_from_users").s()),
                    Integer.parseInt(item1.get("reviews_from_users").s())));

            for (Map<String, AttributeValue> item : filteredItems) {
                String title = item.get("title").s();
                System.out.println(item);
                titles.add(title);
            }

        } catch (DynamoDbException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return titles;

    }



}
