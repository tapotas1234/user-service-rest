package org.tapotas;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.tapotas.configs.AppConfig;
import org.tapotas.controllers.UserController;
import org.tapotas.dto.CreateUserDto;
import org.tapotas.dto.UserDto;
import org.tapotas.services.UserService;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc
@Import({AppConfig.class, TestJpaConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
public class UserEntityControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        CreateUserDto createDto = new CreateUserDto("Test User", 30, "test@example.com");
        UserDto createdUser = new UserDto(1L, "Test User", 30, "test@example.com", LocalDateTime.now(), LocalDateTime.now());

        BDDMockito.given(userService.createUser(ArgumentMatchers.any(CreateUserDto.class))).willReturn(createdUser);

        mockMvc.perform(MockMvcRequestBuilders.post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test User"));

        Mockito.verify(userService).createUser(ArgumentMatchers.any(CreateUserDto.class));
    }

    @Test
    void getUser_ShouldReturnUser() throws Exception {
        UserDto user = new UserDto(1L, "Test User", 30, "test@example.com", LocalDateTime.now(), LocalDateTime.now());
        BDDMockito.given(userService.getUserById(1L)).willReturn(user);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("test@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Test User"));

        Mockito.verify(userService).getUserById(1L);
    }

    @Test
    void getAllUsers_ShouldReturnUsersList() throws Exception {
        List<UserDto> users = Arrays.asList(
                new UserDto(1L, "Test User1", 30, "test1@example.com", LocalDateTime.now(), LocalDateTime.now()),
                new UserDto(2L, "Test User2", 30, "test2@example.com", LocalDateTime.now(), LocalDateTime.now())
        );

        BDDMockito.given(userService.getAllUsers()).willReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[1].id").value(2L));

        Mockito.verify(userService).getAllUsers();
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        UserDto updateDto = new UserDto(1L, "Updated User", 30, "updated@example.com", LocalDateTime.now(), LocalDateTime.now());;
        BDDMockito.given(userService.updateUser(ArgumentMatchers.eq(1L), ArgumentMatchers.any(UserDto.class))).willReturn(updateDto);

        mockMvc.perform(MockMvcRequestBuilders.put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("updated@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.name").value("Updated User"));

        Mockito.verify(userService).updateUser(ArgumentMatchers.eq(1L), ArgumentMatchers.any(UserDto.class));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/api/users/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(userService).deleteUser(1L);
    }

    @Test
    void searchUsers_ShouldReturnMatchingUsers() throws Exception {
        List<UserDto> users = Arrays.asList(
                new UserDto(1L, "Test User", 30, "test@example.com", LocalDateTime.now(), LocalDateTime.now())
        );

        BDDMockito.given(userService.searchUsers("test")).willReturn(users);

        mockMvc.perform(MockMvcRequestBuilders.get("/api/users/search")
                        .param("query", "test"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].id").value(1L));

        Mockito.verify(userService).searchUsers("test");
    }
}
