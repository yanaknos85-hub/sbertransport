export const formatFullName = (
  firstName: string | undefined,
  lastName: string | undefined,
  middleName: string | undefined
) => {
  const formattedFirstName: string = firstName || '';
  const formattedMiddleName: string = middleName || '';
  const formattedLastNameName: string = lastName || '';
  const formattedFullName = `${formattedLastNameName} ${formattedFirstName} ${formattedMiddleName}`;

  return formattedFullName;
};
