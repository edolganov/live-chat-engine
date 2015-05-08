
var ServerErrLabels = {};

ServerErrLabels.en = {
	
	//label by error types
	"och.util.sql.ConcurrentUpdateSqlException" : "Data on the client became outdated. Please reload the page",

	"och.api.exception.user.AccessDeniedException" : "Access Denied",
	"och.api.exception.user.InvalidCaptchaException" : "Invalid captcha",
	"och.api.exception.user.DuplicateUserDataException" : "Login or email already exists",
	"och.api.exception.user.InvalidLoginDataException" : "Invalid email/login or password",
	"och.api.exception.user.NotActivatedUserException" : "User not activated",
	"och.api.exception.user.BannedUserException" : "User banned",
	"och.api.exception.user.UserNotFoundException" : "User not found",
	"och.api.exception.user.InvalidLoginDataForUpdateException" : "Invalid current password",
	"och.api.exception.chat.NotActiveOperatorException" : "Operator is OFFLINE",

	"och.api.exception.chat.AddUserReqAlreadyExistsException" : "alredy exists",
	"och.api.exception.chat.NoAccountChatException" : "Account not found",
	"och.api.exception.chat.UserAlreadyInAccountException" : "Already member of account",
	"och.api.exception.chat.ChatAccountBlockedException" : "Account is blocked. You can not do this operation",
	"och.api.exception.chat.ChatAccountPausedException" : "Account on pause. You can not do this operation",
	"och.api.exception.chat.NoChatAccountException" : "No account by this id",
	"och.api.exception.chat.AccountsLimitException" : "Can't create more new accounts",

	"och.api.exception.tariff.NotPublicTariffException" : "Not public tariff",
	"och.api.exception.tariff.ChangeTariffLimitException" : "Day limit of current changes",
	"och.api.exception.tariff.InvalidTariffException" : "Invalid tariff",
	"och.api.exception.tariff.TariffNotFoundException" : "Tariff not found",
	"och.api.exception.tariff.UpdateTariffOperatorsLimitException" : "Account has too many operators for new tariff. \n\
				<br>You can remove operators <a href='javascript:' onclick='if(Global) Global.trigger(Msg.chats_openUsers);'>here</a>",
	"och.api.exception.tariff.OperatorsLimitException" : "Limit of operators by current tariff. \n\
				<br>If you account's owner - you can change tariff for the removal of restrictions in \n\
				<a href='javascript:' onclick='if(Global) Global.trigger(Msg.chats_openInfo);'>Account Info Tab</a>",
	

	//label by error msgs
	"errorMsg: invalid email" : "Invalid email",
	"errorMsg: empty field 'email'" : "Need enter the email",
	"errorMsg: invalid field 'email': max size" : "Invalid email max size (60)",
	"errorMsg: empty field 'login'" : "Need enter the login",
	"errorMsg: invalid field 'login': max size" : "Invalid login max size (30)",
	"errorMsg: empty field 'psw'" : "Need enter the password",
	"errorMsg: empty field 'captchaResponse'" : "Need enter the captcha",
	"errorMsg: empty field 'captchaChallenge'" : "Need enter the captcha",
	"errorMsg: invalid field 'name': min size: 1" : "Need enter the name",
	"errorMsg: invalid field 'name': max size: 30" : "Limit for name size is 30",
	"errorMsg: empty field 'accId'":"Need enter the account id",
	"errorMsg: " : "",


	"" : ""
};


ServerErrLabels.ru = {

	//label by error types
	"och.util.sql.ConcurrentUpdateSqlException" : "Данные на клиенте устарели. Обновите страницу",

	"och.api.exception.user.AccessDeniedException" : "Доступ запрещен",
	"och.api.exception.user.InvalidCaptchaException" : "Неверный код с картинки (капча)",
	"och.api.exception.user.DuplicateUserDataException" : "Такой логин или email уже существует",
	"och.api.exception.user.InvalidLoginDataException" : "Неверный email/логин или пароль",
	"och.api.exception.user.NotActivatedUserException" : "Пользователь не активирован",
	"och.api.exception.user.BannedUserException" : "Этот пользователь забанен",
	"och.api.exception.user.UserNotFoundException" : "Этот пользователь не найден",
	"och.api.exception.user.InvalidLoginDataForUpdateException" : "Неверный текущий пароль",
	"och.api.exception.chat.NotActiveOperatorException" : "Оператор - OFFLINE",

	"och.api.exception.chat.AddUserReqAlreadyExistsException" : "уже существует",
	"och.api.exception.chat.NoAccountChatException" : "Аккаунт не найден",
	"och.api.exception.chat.UserAlreadyInAccountException" : "Пользователь уже добавлен к аккаунту",
	"och.api.exception.chat.ChatAccountBlockedException" : "Аккаунт заблокирован. Операция не доступна",
	"och.api.exception.chat.ChatAccountPausedException" : "Аккаунт на паузе. Операция не доступна",
	"och.api.exception.chat.NoChatAccountException" : "Не найден аккаунт по данному ID",
	"och.api.exception.chat.AccountsLimitException" : "Достигнут лимит на количество аккаунтов",

	"och.api.exception.tariff.NotPublicTariffException" : "Недоступный тариф",
	"och.api.exception.tariff.ChangeTariffLimitException" : "Достигнут дневной лимит на эту операцию",
	"och.api.exception.tariff.InvalidTariffException" : "Неверный тариф",
	"och.api.exception.tariff.TariffNotFoundException" : "Тариф не найден",
	"och.api.exception.tariff.UpdateTariffOperatorsLimitException" : "Аккаунт содержит слишком много операторов для данного тарифа. \n\
				<br>Вы можете удалить лишнего оператора <a href='javascript:' onclick='if(Global) Global.trigger(Msg.chats_openUsers);'>здесь</a>",
	"och.api.exception.tariff.OperatorsLimitException" : "Тариф не позволяет добавить еще оператора. \n\
				<br>Если вы владелец - вы можете убрать это ограничение, сменив тариф \n\
				<a href='javascript:' onclick='if(Global) Global.trigger(Msg.chats_openInfo);'>здесь</a>",


	"invalidReq":"Ошибка",
	"warnTitle":"Внимание",
	"infoTitle":"Информация",

	//label by error msgs
	"errorMsg: invalid email" : "Неверный email",
	"errorMsg: empty field 'email'" : "Нужно ввести email",
	"errorMsg: invalid field 'email': max size" : "Превышен макс. размер email (60)",
	"errorMsg: empty field 'login'" : "Нужно ввести логин",
	"errorMsg: invalid field 'login': max size" : "Превышен макс. размер логина (30)",
	"errorMsg: empty field 'psw'" : "Нужно ввести пароль",
	"errorMsg: empty field 'captchaResponse'" : "Нужно ввести код с картинки (капчу)",
	"errorMsg: empty field 'captchaChallenge'" : "Нужно ввести код с картинки (капчу)",
	"errorMsg: invalid field 'name': min size: 1" : "Нужно ввести имя",
	"errorMsg: invalid field 'name': max size: 30" : "Максимальная длина имени 30 символов",
	"errorMsg: empty field 'accId'":"Нужно ввести идентификатор аккаунта",
	"errorMsg: " : "",


	"" : ""
};
