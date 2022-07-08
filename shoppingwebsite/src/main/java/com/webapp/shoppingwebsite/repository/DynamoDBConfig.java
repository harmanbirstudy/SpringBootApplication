package com.webapp.shoppingwebsite.repository;

//dynamodb run command
//docker run -p 8000:8000 -v $(pwd)/local/dynamodb:/data/ amazon/dynamodb-local -jar DynamoDBLocal.jar -sharedDb -dbPath /data
//alias aws='docker run --rm -ti -v ~/.aws:/root/.aws -v $(pwd):/aws amazon/aws-cli'
//aws configure .. press emter empty values
//docker ps
//docker inspect "container id of running dynamdo db"
//get IP address and then use in below command while running every dynamodb command
//docker ps and then docker inspect "container id"
//aws dynamodb list-tables --endpoint-url http://172.17.0.2:8000
//alias awsddb='docker run --rm -ti -v ~/.aws:/root/.aws -v $(pwd):/aws amazon/aws-cli dynamodb --endpoint-url http://172.17.0.2:8000'
//awsddb list-tables
//awsddb create-table --cli-input-json file://HarmanData/AWS_Materials/ShoppingWebsite/SpringBootApp/shoppingwebsite/usertable.json
//awsddb delete-table --table-name users
//GUI
////NoSqlWorkbench
//aws configure check if saved last time
//accesskeyid--AKIAWRAUXZ4G434QYNQX
//secretkey -- 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
//User--HarmanbirSinghRai
//email--	kaur262610@gmail.com

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import org.apache.commons.lang3.StringUtils;
import org.socialsignin.spring.data.dynamodb.repository.config.EnableDynamoDBRepositories;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@EnableDynamoDBRepositories
        (basePackages = "com.webapp.shoppingwebsite.repository")
public  class DynamoDBConfig {

    @Value("${amazon.aws.accesskey}")
    private String amazonAWSAccessKey;

    @Value("${amazon.aws.secretkey}")
    private String amazonAWSSecretKey;

    @Value("${amazon.dynamodb.endpoint}")
    private String amazonDynamoDBEndpoint;



    public AWSCredentialsProvider awsCredentialsProvider()
    {
        return new AWSStaticCredentialsProvider(amazonAWSCredentials());
    }

    @Bean
    public AWSCredentials amazonAWSCredentials() {
        return new BasicAWSCredentials(amazonAWSAccessKey, amazonAWSSecretKey);
    }

// local db
//    @Bean
//    public AmazonDynamoDB amazonDynamoDB() {
//        AmazonDynamoDB amazonDynamoDB
//                = new AmazonDynamoDBClient(amazonAWSCredentials());
//
//        if (!StringUtils.isEmpty(amazonDynamoDBEndpoint)) {
//            amazonDynamoDB.setEndpoint(amazonDynamoDBEndpoint);
//        }
//
//        return amazonDynamoDB;
//    }

    @Bean
    public AmazonDynamoDB amazonDynamoDB()
    {
        return AmazonDynamoDBClientBuilder.standard().withCredentials(awsCredentialsProvider()).withRegion(Regions.US_EAST_1).build();
    }
}
