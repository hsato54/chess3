package dataaccess;

import model.UserData;

import java.util.HashSet;
import java.util.Set;

public class MemoryUserDAO implements UserDAO {

    private Set<UserData> users;

    public MemoryUserDAO(){
        users = new HashSet<>();
    }

    @Override
    public void createUser(UserData user) throws DataAccessException{
        if (!users.contains(user)){
            users.add(user);
            return;
        }
        throw new DataAccessException("This User Already Exists.");
    }

    @Override
    public UserData getUser(String username) throws DataAccessException{
        for (UserData user : users){
            if (user.username().equals(username)){
                return user;
            }
        }
        return null;
    }

    @Override
    public void updateUser(UserData updatedUser){
        users.removeIf(user -> user.username().equals(updatedUser.username()));
        users.add(updatedUser);
    }

    @Override
    public void deleteUser(String username){
        users.removeIf(user -> user.username().equals(username));
    }

    @Override
    public void clear(){
        users.clear();
    }



}
