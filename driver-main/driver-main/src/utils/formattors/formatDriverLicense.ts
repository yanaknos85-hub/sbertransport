export const formatDriverLicense = (str = '') => {
  const initialValue = str.replace(/[^\d]/g, '');

  const match = initialValue.match(/^(\d{2})(\d{2})(\d{6})$/);

  if (match) {
    return `${match[1]} ${match[2]} ${match[3]}`;
  }

  return initialValue;
};
