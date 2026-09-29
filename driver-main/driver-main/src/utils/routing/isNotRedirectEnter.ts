import { routes } from 'constants/routes.constants';

// так как наш роутинг построен на том, что любая не авторизованная страница шлет на url '/',
// было невозможно положить в роутинг любую неавторизованную страницу, кроме адреса '/', поэтому я сделал эту функцию
// которая делает исключения в проверках на аторизацию.
export function isNotDirectEnter(url: string): boolean {
  return url !== routes.Success && url !== routes.Failure;
}
