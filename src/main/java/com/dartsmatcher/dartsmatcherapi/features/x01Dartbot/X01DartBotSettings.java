package com.dartsmatcher.dartsmatcherapi.features.x01Dartbot;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01DartBotSettings {

	private int expectedThreeDartAverage;

}
