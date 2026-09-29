export const formatPhoneNumber = (phoneNumber: string) => {
  const cleanedNumber = phoneNumber.replace(/\D/g, '');
  const groups = cleanedNumber.match(/^(\d{1})(\d{3})(\d{3})(\d{2})(\d{2})$/);
  if (!groups) {
    return phoneNumber;
  }
  const formattedNumber = `8 ${groups.slice(2).join(' ')}`;

  return formattedNumber;
};
