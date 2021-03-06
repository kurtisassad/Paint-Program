
#include "WindowsProc.h"


JNIEXPORT void JNICALL Java_ca_utoronto_utm_pointer_WindowsPointer_init(JNIEnv *e, jobject o, jlong lhWnd) 
{
	JavaVM *jvm;

	e->GetJavaVM(&jvm);
	WindowsProc::windObjects[(HWND)lhWnd] = new WindowsProc(jvm, e->NewGlobalRef(o), 
								(WNDPROC)GetWindowLongPtr((HWND)lhWnd, GWLP_WNDPROC));

	SetWindowLongPtr((HWND)lhWnd, GWLP_WNDPROC, (LONG_PTR)WindowsProc::PtrSetup);
}



WindowsProc::WindowsProc(JavaVM * jvm, jobject object, WNDPROC wndprocOrig)
{
	this->jvm = jvm;
	this->pairedObject = object;
	this->wndprocOrig = wndprocOrig;
}

std::map <HWND, WindowsProc*> WindowsProc::windObjects;

LRESULT CALLBACK WindowsProc::_PtrSetup(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam) {
	std::thread msg(&WindowsProc::getMethods, this);
	msg.detach();
	RegisterPointerDeviceNotifications(hWnd, 0);
	EnableMouseInPointer(true);
	SetWindowLongPtr(hWnd, GWLP_WNDPROC, (LONG_PTR)&WndProcProxy);
	return CallWindowProc(wndprocOrig, hWnd, message, wParam, lParam);
}

LRESULT CALLBACK WindowsProc::_WndProcProxy(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	int eventId = 0;
	switch (message) {
	case WM_KEYDOWN:
	case WM_SYSKEYDOWN:
		eventId = JKEY_PRESS;
		goto key;
	case WM_KEYUP:
	case WM_SYSKEYUP:
		eventId = JKEY_RELEASE;
		goto key;
	key:
		{
			std::thread msg(&WindowsProc::sendKeyUpdate,this, eventId,getModifiers(), wParam);
			msg.detach();

			return CallWindowProc(wndprocOrig, hWnd, message, wParam, lParam);
		}
	case WM_POINTERLEAVE:
		eventId = JMOUSE_EXITED;
		goto process;
	case WM_POINTERDOWN:
		eventId = JMOUSE_PRESSED;
		goto process;
	case WM_POINTERUP:
		eventId = JMOUSE_RELEASED;
		goto process;
	case WM_POINTERENTER:
		eventId = JMOUSE_ENTERED;
		goto process;
	case WM_POINTERUPDATE:
		eventId = JMOUSE_MOVED;
		goto process;
	process:
		{
			POINTER_PEN_INFO penInfo;
			POINTER_INFO pointerInfo;
			UINT32 pointerId = GET_POINTERID_WPARAM(wParam);
			POINTER_INPUT_TYPE pointerType = PT_POINTER;
			int pressure = 0;

			if (!GetPointerType(pointerId, &pointerType))
			{
				pointerType = PT_POINTER;
			}
			if (pointerType == PT_PEN) {
				if (GetPointerPenInfo(pointerId, &penInfo)) {
					pointerInfo = penInfo.pointerInfo;
					pressure = penInfo.pressure;
				}
				else {
					break;
				}

			}
			else if (!GetPointerInfo(pointerId, &pointerInfo)) {
				break;
			}

			std::thread msg(&WindowsProc::sendUpdate, this, eventId, pointerInfo, getModifiers(), pressure);
			msg.detach();

			return CallWindowProc(wndprocOrig, hWnd, message, wParam, lParam);
		}
	}
	return CallWindowProc(wndprocOrig, hWnd, message, wParam, lParam);
}

void WindowsProc::getMethods()
{
	JNIEnv *env;
	jclass thisClass;

	jvm->AttachCurrentThread((void **)&env, NULL);

	thisClass = env->GetObjectClass(pairedObject);

	update = env->GetMethodID(thisClass, "update", "(IJIIIIIII)V");
	key = env->GetMethodID(thisClass, "keyUpdate", "(IJIIC)V");

	jvm->DetachCurrentThread();
}

void WindowsProc::sendKeyUpdate(int eventId, int modifiers, WPARAM wParam) {
	JNIEnv *env;
	jvm->AttachCurrentThread((void **)&env, NULL);

	env->CallVoidMethod(pairedObject, key,
		eventId, 0, modifiers, wParam, MapVirtualKey(wParam, MAPVK_VK_TO_CHAR));

	jvm->DetachCurrentThread();
}

void WindowsProc::sendUpdate(int eventId, POINTER_INFO pointerInfo, int modifiers, int pressure) {
	JNIEnv *env;
	jvm->AttachCurrentThread((void **)&env, NULL);

	env->CallVoidMethod(pairedObject, update,
		eventId, pointerInfo.dwTime, modifiers, pointerInfo.ptPixelLocation.x,
		pointerInfo.ptPixelLocation.y, pointerInfo.historyCount, getButton(pointerInfo.pointerFlags),
		pointerInfo.pointerId, pressure);

	jvm->DetachCurrentThread();
}


jint WindowsProc::getButton(POINTER_FLAGS flags)
{
	if (flags & POINTER_FLAG_FIRSTBUTTON)
		return 1;
	if (flags & POINTER_FLAG_SECONDBUTTON)
		return 3;
	if (flags & POINTER_FLAG_THIRDBUTTON)
		return 2;
	return 0;
}

jint WindowsProc::getModifiers()
{
	int rtn = 0;
	rtn |= HIBYTE(GetKeyState(VK_CONTROL)) ? CONTROL : 0;
	rtn |= HIBYTE(GetKeyState(VK_MENU)) ? ALT : 0;
	rtn |= HIBYTE(GetKeyState(VK_SHIFT)) ? SHIFT : 0;
	rtn |= HIBYTE(GetKeyState(VK_LBUTTON)) ? LBUTTON : 0;
	rtn |= HIBYTE(GetKeyState(VK_RBUTTON)) ? RBUTTON : 0;
	rtn |= HIBYTE(GetKeyState(VK_MBUTTON)) ? RBUTTON : 0;
	return rtn;
}

LRESULT WindowsProc::PtrSetup(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	return windObjects[hWnd]->_PtrSetup(hWnd, message, wParam, lParam);
}

LRESULT WindowsProc::WndProcProxy(HWND hWnd, UINT message, WPARAM wParam, LPARAM lParam)
{
	return windObjects[hWnd]->_WndProcProxy(hWnd, message, wParam, lParam);
}

WindowsProc::~WindowsProc()
{
	JNIEnv *env;
	jvm->AttachCurrentThread((void **)&env, NULL);

	env->DeleteGlobalRef(pairedObject);

	jvm->DetachCurrentThread();
}