package com.audio.transcriber;

import java.io.File;
import java.io.IOException;

import org.springframework.ai.audio.transcription.AudioTranscriptionPrompt;
import org.springframework.ai.audio.transcription.AudioTranscriptionResponse;
import org.springframework.ai.openai.OpenAiAudioTranscriptionModel;
import org.springframework.ai.openai.OpenAiAudioTranscriptionOptions;
import org.springframework.ai.openai.api.OpenAiAudioApi;
import org.springframework.ai.openai.api.OpenAiAudioApi.TranscriptResponseFormat;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TranscriptionController {

	private final OpenAiAudioTranscriptionModel openAiTranscriptionModel;
	
	public TranscriptionController() {
		OpenAiAudioApi openAiAudioApi = new OpenAiAudioApi(System.getenv("spring.ai.openai.api-key"));
		this.openAiTranscriptionModel = new OpenAiAudioTranscriptionModel(openAiAudioApi);
	}
	
	@PostMapping("/api/transcribe")
	public ResponseEntity<String> transcribeAudio(
			@RequestParam("file") MultipartFile file) throws IOException{
//		create temp file and write audio in temp
		File tempFile = File.createTempFile("audio", "wav");
		file.transferTo(tempFile);
		
		var transcriptionOptions = OpenAiAudioTranscriptionOptions.builder()
			    .withResponseFormat(TranscriptResponseFormat.TEXT)
			    .withLanguage("en")
			    .withTemperature(0f)
			    .build();

			var audioFile = new FileSystemResource(tempFile);
			AudioTranscriptionPrompt transcriptionRequest = new AudioTranscriptionPrompt(audioFile, transcriptionOptions);
			AudioTranscriptionResponse response = openAiTranscriptionModel.call(transcriptionRequest);
			
//			delete temp file
			tempFile.delete();
			return new ResponseEntity<>(response.getResult().getOutput(),HttpStatus.OK);
			
	}
}
