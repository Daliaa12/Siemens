package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.siemens.internship.model.Item;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Item validItem;

    @BeforeEach
    void setup() {
        validItem = new Item();
        validItem.setName("Test Item");
        validItem.setDescription("Some Description");
        validItem.setEmail("valid@example.com");
    }

    @Test
    void testCreateValidItem() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value("valid@example.com"));
    }

    @Test
    void testCreateInvalidEmailItem() throws Exception {
        validItem.setEmail("invalid-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetAllItems() throws Exception {
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void testUpdateExistingItem() throws Exception {
        String content = objectMapper.writeValueAsString(validItem);

        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andReturn().getResponse().getContentAsString();

        Item created = objectMapper.readValue(response, Item.class);
        created.setName("Updated Name");

        mockMvc.perform(put("/api/items/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(created)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void testDeleteItem() throws Exception {
        String content = objectMapper.writeValueAsString(validItem);

        String response = mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content))
                .andReturn().getResponse().getContentAsString();

        Item created = objectMapper.readValue(response, Item.class);

        mockMvc.perform(delete("/api/items/" + created.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void testProcessItemsAsync() throws Exception {
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isCreated());

        MvcResult asyncResult = mockMvc.perform(get("/api/items/process"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(asyncResult))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].status").value("PROCESSED"));
    }
    @Test
    void testUpdateNonExistingItem() throws Exception {
        validItem.setId(9999L);
        validItem.setName("New Name");

        mockMvc.perform(put("/api/items/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Item with ID 9999 not found."));
    }
    @Test
    void testDeleteNonExistingItem() throws Exception {
        mockMvc.perform(delete("/api/items/9999"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Item with ID 9999 not found."));
    }
    @Test
    void testCreateItemWithUnusualButValidEmail() throws Exception {
        validItem.setEmail("user+test@email.gmail.com");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("user+test@email.gmail.com"));
    }


}
