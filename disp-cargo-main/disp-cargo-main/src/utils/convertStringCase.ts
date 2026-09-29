export const convertCamelToSnakeCase = (string: string, uppercase = true, splitter = '_') => {
  const snakeCaseString = string.split(/(?=[A-Z])/).join(splitter);

  return uppercase ? snakeCaseString.toUpperCase() : snakeCaseString;
};

export const convertSnakeToCamelCase = (string: string) => (
  string.toLowerCase().replace(/([-_][a-z])/g, group => (
    group
      .toUpperCase()
      .replace('-', '')
      .replace('_', '')
  ))
);
