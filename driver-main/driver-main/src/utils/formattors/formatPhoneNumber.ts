export const formatPhoneNumber = (inputPhone: string) => {
  const cleaned = ('' + inputPhone).replace(/[^\d]/g, '');
  let defaultNumber = cleaned;

  if (cleaned.substring(0, 1) !== '7') {
    defaultNumber = '7' + defaultNumber;
  }

  const match = defaultNumber.match(/^(\d{1})(\d{0,3})(\d{0,3})(\d{0,2})(\d{0,2})$/);

  if (match) {
    let formatted = `+${match[1]}`;

    if (match[2]) formatted += ` ${match[3] ? '(' : ''}${match[2]}${match[3] ? ')' : ''}`;

    if (match[3]) formatted += ` ${match[3]}`;

    if (match[4]) formatted += ` ${match[4]}`;

    if (match[5]) formatted += ` ${match[5]}`;

    return formatted;
  }

  return defaultNumber;
};
