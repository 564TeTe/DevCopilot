package com.devcopilot.infrastructure.config;

import com.devcopilot.infrastructure.mq.RabbitNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableRabbit
public class RabbitMqConfig {

    @Bean
    public DirectExchange taskExchange() {
        return new DirectExchange(RabbitNames.TASK_EXCHANGE, true, false);
    }

    @Bean
    public Queue documentTaskQueue() {
        return new Queue(RabbitNames.DOCUMENT_QUEUE, true);
    }

    @Bean
    public Queue codeTaskQueue() {
        return new Queue(RabbitNames.CODE_QUEUE, true);
    }

    @Bean
    public Queue prTaskQueue() {
        return new Queue(RabbitNames.PR_QUEUE, true);
    }

    @Bean
    public Binding documentBinding(DirectExchange taskExchange, Queue documentTaskQueue) {
        return BindingBuilder.bind(documentTaskQueue).to(taskExchange).with(RabbitNames.DOCUMENT_ROUTING_KEY);
    }

    @Bean
    public Binding codeBinding(DirectExchange taskExchange, Queue codeTaskQueue) {
        return BindingBuilder.bind(codeTaskQueue).to(taskExchange).with(RabbitNames.CODE_ROUTING_KEY);
    }

    @Bean
    public Binding prBinding(DirectExchange taskExchange, Queue prTaskQueue) {
        return BindingBuilder.bind(prTaskQueue).to(taskExchange).with(RabbitNames.PR_ROUTING_KEY);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false);
        return factory;
    }
}
