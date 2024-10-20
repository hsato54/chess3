package dataaccess;

import Model.UserData;

import java.util.HashSet;

public class MemoryUserDAO { //implements UserDAO
        void createUser(UserData user);
        UserData getUser(UserData user);
        void updateUser(UserData user);
        void deleteUser(String username);
        void clear();



}
