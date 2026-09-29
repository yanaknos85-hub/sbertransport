export const formatterPhoneNumber = (phone: string | undefined) => phone && !phone.startsWith('+') ? '+' + phone.replace(/[()\s]+/g, '') : phone?.replace(/[()\s]+/g, '');

export const internalNotePattern = /^[А-Я]{2,3}-\d{1,3}(?:-\d{1,2})?-\вн\/\d{1,3}$/; // eslint-disable-line no-useless-escape
