export const parseNumber = (tel: string | undefined) => {
  if (tel) {
    const cleaned = `${tel}`.replace(/\D/g, '');
    const match = cleaned.match(/^(\d{1})(\d{3})(\d{3})(\d{2})(\d{2})$/);
    if (!match) {
      return tel;
    }
    const formatted = `+${match[1]} ${match[2]}-${match[3]}-${match[4]}-${match[5]}`;
    return formatted;
  }

  return '';
};
