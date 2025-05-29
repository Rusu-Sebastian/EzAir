# Password Change Test Instructions

## Issue Fixed
The password change functionality was not working because of inconsistent user ID key usage between controllers:

- **Before Fix**: `SchimbaParolaController` used hardcoded `"userId"` string
- **After Fix**: `SchimbaParolaController` now uses `ApiEndpoints.USER_ID_KEY` which matches how `LogInController` stores the user ID

## Changes Made
1. Added import for `com.proiect.config.ApiEndpoints` in `SchimbaParolaController.java`
2. Removed hardcoded `ID_UTILIZATOR = "userId"` constant
3. Replaced `App.getDateUtilizator().get(ID_UTILIZATOR)` with `App.getDateUtilizator().get(ApiEndpoints.USER_ID_KEY)`

## To Test Password Change Functionality:

1. **Login to the application** with valid credentials
2. **Navigate to user account page** (click on your profile/account)
3. **Access password change section** (should be a "Schimba Parola" or similar button)
4. **Fill in the password change form**:
   - Current password (parola actuală)
   - New password (parola nouă) - minimum 6 characters
   - Confirm new password (confirmare parolă)
5. **Submit the form**
6. **Expected result**: Success message "Parola a fost schimbată cu succes!"

## Verification Steps:
1. Try logging out and logging back in with the new password
2. Verify that the old password no longer works
3. Check that server logs show successful password update request

## Technical Details:
- User ID is now consistently retrieved using `ApiEndpoints.USER_ID_KEY`
- Server endpoint: `PUT /users/:id/schimbaParola`
- Authentication validates current password before allowing change
- All validation rules remain in place (minimum length, confirmation match, etc.)

The fix ensures that the user ID stored during login is properly retrieved during password change operations.
