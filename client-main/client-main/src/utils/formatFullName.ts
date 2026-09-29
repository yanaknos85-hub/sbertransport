export const formatFullName = (
  firstName: string | undefined,
  lastName: string | undefined,
  middleName: string | undefined
) => {
  const formattedFirstName: string = firstName ? `${firstName?.charAt(0)}.` : '';
  const formattedMiddleName: string = middleName ? `${middleName.charAt(0)}.` : '';
  const formattedLastNameName: string = lastName || '';
  const formattedFullName = `${formattedLastNameName} ${formattedFirstName}${formattedMiddleName}`;

  return formattedFullName;
};
