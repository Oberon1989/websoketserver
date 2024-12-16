package ru.webdevpet.server.dto;


import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "messageType" // Тип будет определяться этим полем в JSON
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = BroadcastMessage.class, name = "broadcast"),
        @JsonSubTypes.Type(value = UnicastMessage.class, name = "unicast"),
        @JsonSubTypes.Type(value = RequestId.class, name = "requestId")

})
public interface Message {

}
