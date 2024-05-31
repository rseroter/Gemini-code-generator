package com.seroter.demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.vertexai.gemini.VertexAiGeminiChatModel;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.ai.chat.messages.Media;


@Component
public class CodeGenerator implements ApplicationListener<ApplicationReadyEvent> {

    //member variables
    private VertexAiGeminiChatModel chatClient;
    private PromptContext promptData;
    private String appFolder;
    private String promptFile;

    //constructor that's loaded up with Vertex and CLI argument objects
    //@Autowired
    public CodeGenerator(VertexAiGeminiChatModel chatClient, ApplicationArguments appArgs) {
        this.chatClient = chatClient;
        List<String> args = appArgs.getOptionValues("prompt-file");
        
        //checked earlier to ensure that the argument list isn't empty
        promptFile = args.get(0);
        System.out.println("Prompt file is: " + promptFile);
        
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent applicationReadyEvent) {
        System.out.println("App is ready");

        //instantiate objects based on the local JSON file
        JsonPromptLoader();

        System.out.println("Prompt count is: " + promptData.getPrompts().size());

        //foreach block that loops through the list of prompts and calls getGeneratedText for each item
        for (PromptData appPrompt : promptData.getPrompts()) {
            getGeneratedText(appPrompt);
        }   
    }

    //function that loads the referenced JSON file and instantiates a Java object
    public void JsonPromptLoader() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            promptData = objectMapper.readValue(new File(promptFile), PromptContext.class);
            appFolder = promptData.getFolder();
            System.out.println("Folder: " + appFolder);
        } catch (FileNotFoundException e) {
            System.err.println("Prompt file not found: " + promptFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //main operation that coordinates calls to LLM and writes the code out to disk
    private void getGeneratedText(PromptData p) {
        String folderPath = appFolder;
        String fileName = p.getFileName();
        String filePath = createFilePath(folderPath, fileName);
        String prompt = p.getPrompt();
    
        Optional<List<Media>> localCode = getLocalCode();
    
        String extractedValue;
        if (localCode.isPresent() && localCode.get().size() > 0) {
            extractedValue = callLlmWithLocalCode(prompt, localCode.get());
        } else {
            extractedValue = callLlmWithoutLocalCode(prompt);
        }
    
        writeCodeToFile(filePath, extractedValue);
    }
    
    //create the file path for the generated code
    private String createFilePath(String folderPath, String fileName) {
        // Create the folder if it doesn't exist
        File folder = new File(folderPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }
    
        return folderPath + File.separator + fileName;
    }
    
    //call the LLM and pass in existing code
    private String callLlmWithLocalCode(String prompt, List<Media> localCode) {
        System.out.println("calling LLM with local code");
        var userMessage = new UserMessage(prompt, localCode);
        var response = chatClient.call(new Prompt(List.of(userMessage)));
        return extractCodeContent(response.toString());
    }
    
    //call the LLM when there's no local code
    private String callLlmWithoutLocalCode(String prompt) {
        System.out.println("calling LLM withOUT local code");
        var response = chatClient.call(prompt);
        return extractCodeContent(response.toString());
    }
    
    //write the final code to the target file path
    private void writeCodeToFile(String filePath, String codeContent) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
    
            FileWriter writer = new FileWriter(file);
            writer.write(codeContent);
            writer.close();
    
            System.out.println("Content written to file: " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    //method that extracts code from the LLM response
    public static String extractCodeContent(String markdown) {

        System.out.println("Markdown: " + markdown);

        String regex = "`(\\w+)?\\n([\\s\\S]*?)```";  
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(markdown);

        if (matcher.find()) {
            String codeContent = matcher.group(2); // Extract group 2 (code content)
            return codeContent;
        } else {
            //System.out.println("No code fence found.");
            return markdown;
        }
    }

    //load code from any existing files in the folder
    private Optional<List<Media>> getLocalCode() {
        String directoryPath = appFolder;
        File directory = new File(directoryPath);
    
        if (!directory.exists()) {
            System.out.println("Directory does not exist: " + directoryPath);
            return Optional.empty();
        }
    
        try {
            return Optional.of(Arrays.stream(directory.listFiles())
                .filter(File::isFile)
                .map(file -> {
                    try {
                        byte[] codeContent = Files.readAllLines(file.toPath())
                            .stream()
                            .collect(Collectors.joining("\n"))
                            .getBytes();
                        return new Media(MimeTypeUtils.TEXT_PLAIN, codeContent);
                    } catch (IOException e) {
                        System.out.println("Error reading file: " + file.getName());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        } catch (Exception e) {
            System.out.println("Error getting local code");
            return Optional.empty();
        }
    } 
}
