package com.redepatas.api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker // Habilita o servidor de mensagens WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  /**
   * Este método registra o endpoint que os clientes usarão para se conectar.
   * Pense nele como a "porta de entrada" para o mundo WebSocket.
   */
  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    // O endpoint é "/ws".
    // setAllowedOriginPatterns("*") permite conexões de qualquer origem (útil para
    // desenvolvimento).
    // withSockJS() oferece uma alternativa para navegadores que não suportam
    // WebSockets nativamente.
    registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
  }

  /**
   * Este método configura o "roteador" de mensagens.
   * Ele define para onde as mensagens vão.
   */
  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    // Define o prefixo para os destinos que vêm DO CLIENTE PARA O SERVIDOR.
    // Ex: um cliente enviaria uma mensagem para "/app/chat".
    registry.setApplicationDestinationPrefixes("/app");

    // Define o prefixo para os destinos que vão DO SERVIDOR PARA O CLIENTE
    // (broadcast).
    // O broker simples em memória vai lidar com todos os destinos que começam com
    // "/topic".
    // Pense em "/topic" como uma estação de rádio pública.
    registry.enableSimpleBroker("/topic");
  }
}