const authSessionStorage: Storage = (window as Window).sessionStorage;

enum Keys {
  previousPath = 'previousPath',
}

type KeyType = keyof typeof Keys;

const set: (key: KeyType, data: string) => void = (key: KeyType, data: string): void => {
  if (data) {
    authSessionStorage.setItem(key, data);
  }
};

const get: (key: KeyType) => string | null = (key: KeyType): string | null => authSessionStorage.getItem(key);

const remove: (key: KeyType) => void = (key: KeyType): void => authSessionStorage.removeItem(key);

export {
  set, get, remove, Keys
};
