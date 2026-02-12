package com.example.auth.PetTests;


import com.example.auth.Pet.PetController;
import com.example.auth.Pet.PetService;
import com.example.auth.user.services.UserService;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(PetController.class)
public class PetControllerTests {

    @Mock
    private PetService petService;
    @Mock
    private UserService userService;
    @Autowired
    private MockMvc mockMvc;

}
