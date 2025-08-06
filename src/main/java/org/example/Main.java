package org.example;

import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        //TIP Press <shortcut actionId="ShowIntentionActions"/> with your caret at the highlighted text
        // to see how IntelliJ IDEA suggests fixing it.
        Scanner input = new Scanner(System.in);
        String token;
        String tenant;
        int pageNumber;
        int size;

        String filename = "output.csv";

        System.out.println("Enter your PactFlow read/write token");
        token = input.nextLine();
        System.out.println("Enter your PactFlow tenant (without URL) format: tenant.pactflow.io");
        tenant = input.nextLine();
        System.out.println("Enter page number to return");
        pageNumber = input.nextInt();
        System.out.println("Enter page size/number of results per page");
        size = input.nextInt();
        input.nextLine();

        ApiResponse users = GetUsers(token, tenant, pageNumber, size);

        if (users == null){
            System.out.println("Users object is null/invalid response");
        }else{
            System.out.println("Enter file name");
            filename = input.nextLine();
            filename += ".csv";
            SaveUsersToFile(users, filename);
        }
        input.close();
    }

    public static ApiResponse GetUsers(String token, String tenant, int pageNumber, int size){
        String URL = String.format("https://%s.pactflow.io/admin/users?page=%d&size=%d", tenant, pageNumber, size);

        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(URL))
                .headers(
                        "Accept", "application/json+hal",
                        "Authorization", "Bearer " + token
                )
                .GET()
                .build();

        HttpResponse<String> response;
        ApiResponse apiResponse = new ApiResponse();
        try{
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            Gson gson = new Gson();
            apiResponse = gson.fromJson(response.body(), ApiResponse.class);
            httpClient.close();
        }catch(IOException | InterruptedException exception){
            System.out.println(exception.getMessage());
        }

        return apiResponse;
    }

    public static void SaveUsersToFile(ApiResponse apiResponse, String filename){
        try(FileWriter fileWriter = new FileWriter(filename)){

            System.out.println("Total count: " + apiResponse.page.totalElements + "\n");
            fileWriter.write("Name,Email,Active?,Roles,Teams,Created,Updated\n");
            ArrayList<String> roles = new ArrayList<String>();
            ArrayList<String> teams = new ArrayList<String>();
            for (int i = 0; i < apiResponse.page.totalElements; i++){
                String line = String.format("%s,%s,%s",
                        apiResponse.users[i].name,
                        apiResponse.users[i].email,
                        apiResponse.users[i].active

                        );
                for (int j = 0; j < apiResponse.users[i]._embedded.roles.length; j++){
                    roles.add(apiResponse.users[i]._embedded.roles[j].name);
                }
                for (int k = 0; k < apiResponse.users[i]._embedded.teams.length; k++){
                    teams.add(apiResponse.users[i]._embedded.teams[k].name);
                }
                line += String.format("%s,%s,%s,%s",
                        roles,
                        teams,
                        apiResponse.users[i].createdAt,
                        apiResponse.users[i].updatedAt
                );
                fileWriter.write(line);
            }
            fileWriter.close();
            System.out.println("File saved to: " + filename);
        }catch(IOException exception){
            System.out.println("Error writing file: " + exception.getMessage());
        }
    }
}