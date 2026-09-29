export const formatPhoneNumber = (str: string | undefined): string => {
  const cleaned = `${str}`.replace(/\D/g, '');
  const match = cleaned.match(/^(7|)?(\d{3})(\d{3})(\d{2})(\d{2})$/);
  if (match) {
    const intlCode = match[1] ? '+7 ' : '';
    return [intlCode, '(', match[2], ') ', match[3], '-', match[4], '-', match[5]].join('');
  }
  return '-';
};
