// eslint-disable-next-line @typescript-eslint/no-explicit-any
type ResultFn = (args: Record<string, any>) => string;

export const inject = (parts: TemplateStringsArray): ResultFn => {
  const originalString = parts.join('');
  const variableNames = (originalString.match(/{{[a-zA-Z_$]+[a-zA-Z_$0-9]*}}/g) || []).map(s => s.replace(/[{}]/g, ''));

  return args => variableNames.reduce(
    (injected, variableName) => Object.prototype.hasOwnProperty.call(args, variableName)
      ? injected.replace(new RegExp(`{{${variableName}}}`, 'g'), args[variableName])
      : injected,
    originalString
  );
};
