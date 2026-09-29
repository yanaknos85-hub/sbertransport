import { User } from '../Employee.interface';

export class UserModel implements Partial<User> {
  id?: string;

  firstName?: string;

  lastName?: string;

  patronymic?: string;

  login?: string;

  password?: string;

  constructor(user: Partial<User>) {
    this.id = user.id;
    this.firstName = user.firstName;
    this.lastName = user.lastName;
    this.patronymic = user.patronymic;
    this.login = user.login;
    this.password = user.password;
  }
}
