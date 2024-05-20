#pragma once
#include "../deps/UnityResolve/UnityResolve.hpp"
#include "Log.h"
#include <memory>

namespace Il2cppUtils {
    using namespace GakumasLocal;

    struct Il2CppClassHead {
        // The following fields are always valid for a Il2CppClass structure
        const void* image;
        void* gc_desc;
        const char* name;
        const char* namespaze;
    };

    struct MethodInfo {
        uintptr_t methodPointer;
        uintptr_t invoker_method;
        const char* name;
        uintptr_t klass;
        //const Il2CppType* return_type;
        //const ParameterInfo* parameters;
        const void* return_type;
        const void* parameters;
        uintptr_t methodDefinition;
        uintptr_t genericContainer;
        uint32_t token;
        uint16_t flags;
        uint16_t iflags;
        uint16_t slot;
        uint8_t parameters_count;
        uint8_t is_generic : 1;
        uint8_t is_inflated : 1;
        uint8_t wrapper_type : 1;
        uint8_t is_marshaled_from_native : 1;
    };

    UnityResolve::Class* GetClass(const std::string& assemblyName, const std::string& nameSpaceName,
                   const std::string& className) {
        const auto assembly = UnityResolve::Get(assemblyName);
        if (!assembly) {
            Log::ErrorFmt("GetMethodPointer error: assembly %s not found.", assemblyName.c_str());
            return nullptr;
        }
        const auto pClass = assembly->Get(className, nameSpaceName);
        if (!pClass) {
            Log::ErrorFmt("GetMethodPointer error: Class %s::%s not found.", nameSpaceName.c_str(), className.c_str());
            return nullptr;
        }
        return pClass;
    }
    /*
    UnityResolve::Method* GetMethodIl2cpp(const char* assemblyName, const char* nameSpaceName,
                                    const char* className, const char* methodName, const int argsCount) {
        auto domain = UnityResolve::Invoke<void*>("il2cpp_domain_get");
        UnityResolve::Invoke<void*>("il2cpp_thread_attach", domain);
        auto image = UnityResolve::Invoke<void*>("il2cpp_assembly_get_image", domain);
        if (!image) {
            Log::ErrorFmt("GetMethodIl2cpp error: assembly %s not found.", assemblyName);
            return nullptr;
        }
        Log::Debug("GetMethodIl2cpp 1");
        auto klass = UnityResolve::Invoke<void*>("il2cpp_class_from_name", image, nameSpaceName, className);
        if (!klass) {
            Log::ErrorFmt("GetMethodIl2cpp error: Class %s::%s not found.", nameSpaceName, className);
            return nullptr;
        }
        Log::Debug("GetMethodIl2cpp 2");
        auto ret = UnityResolve::Invoke<UnityResolve::Method*>("il2cpp_class_get_method_from_name", klass, methodName, argsCount);
        if (!ret) {
            Log::ErrorFmt("GetMethodIl2cpp error: method %s::%s.%s not found.", nameSpaceName, className, methodName);
            return nullptr;
        }
        return ret;
    }*/

    UnityResolve::Method* GetMethod(const std::string& assemblyName, const std::string& nameSpaceName,
                           const std::string& className, const std::string& methodName, const std::vector<std::string>& args = {}) {
        const auto assembly = UnityResolve::Get(assemblyName);
        if (!assembly) {
            Log::ErrorFmt("GetMethod error: assembly %s not found.", assemblyName.c_str());
            return nullptr;
        }
        const auto pClass = assembly->Get(className, nameSpaceName);
        if (!pClass) {
            Log::ErrorFmt("GetMethod error: Class %s::%s not found.", nameSpaceName.c_str(), className.c_str());
            return nullptr;
        }
        auto method = pClass->Get<UnityResolve::Method>(methodName, args);
        if (!method) {
            /*
            method = GetMethodIl2cpp(assemblyName.c_str(), nameSpaceName.c_str(), className.c_str(),
                                     methodName.c_str(), args.size() == 0 ? -1 : args.size());
            if (!method) {
                Log::ErrorFmt("GetMethod error: method %s::%s.%s not found.", nameSpaceName.c_str(), className.c_str(), methodName.c_str());
                return nullptr;
            }*/
            Log::ErrorFmt("GetMethod error: method %s::%s.%s not found.", nameSpaceName.c_str(), className.c_str(), methodName.c_str());
            return nullptr;
        }
        return method;
    }

    void* GetMethodPointer(const std::string& assemblyName, const std::string& nameSpaceName,
                           const std::string& className, const std::string& methodName, const std::vector<std::string>& args = {}) {
        auto method = GetMethod(assemblyName, nameSpaceName, className, methodName, args);
        if (method) {
            return method->function;
        }
        return nullptr;
    }

    void* il2cpp_resolve_icall(const char* s) {
        return UnityResolve::Invoke<void*>("il2cpp_resolve_icall", s);
    }

    Il2CppClassHead* get_class_from_instance(const void* instance) {
        return static_cast<Il2CppClassHead*>(*static_cast<void* const*>(std::assume_aligned<alignof(void*)>(instance)));
    }


    void* find_nested_class(void* klass, std::predicate<void*> auto&& predicate)
    {
        void* iter{};
        while (const auto curNestedClass = UnityResolve::Invoke<void*>("il2cpp_class_get_nested_types", klass, &iter))
        {
            if (static_cast<decltype(predicate)>(predicate)(curNestedClass))
            {
                return curNestedClass;
            }
        }

        return nullptr;
    }

    void* find_nested_class_from_name(void* klass, const char* name)
    {
        return find_nested_class(klass, [name = std::string_view(name)](void* nestedClass) {
            return static_cast<Il2CppClassHead*>(nestedClass)->name == name;
        });
    }

}
